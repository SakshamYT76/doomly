package com.example.tracking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.data.DoomlyDatabase
import com.example.data.DoomlyPreferences
import com.example.data.DoomlyRepository
import com.example.data.PlatformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * DOOMLY's transparent, privacy-first AccessibilityService.
 *
 * HONESTY & PRIVACY GUARANTEES:
 * - Only observes `TYPE_VIEW_SCROLLED` and `TYPE_WINDOW_STATE_CHANGED` for Instagram, YouTube, and TikTok.
 * - `canRetrieveWindowContent="false"` is enforced in XML, and this service NEVER inspects text,
 *   contentDescription, usernames, comments, or messages.
 * - Uses conservative short-video feed heuristics so non-feed scrolls (comment lists, horizontal
 *   carousels, profile grids, or ambiguous containers) are ignored.
 * - Saves only scroll counts and timestamps locally via [DoomlyRepository].
 */
class DoomlyAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var repository: DoomlyRepository
    private lateinit var preferences: DoomlyPreferences
    private lateinit var notificationManager: MilestoneNotificationManager

    // Tracks last counted scroll timestamp per platform to debounce multi-event swipe gestures
    private val lastCountedScrollMsByPlatform = mutableMapOf<PlatformType, Long>()

    // Tracks last known item index per platform container to detect full-page vertical snapping
    private val lastFromIndexByPlatform = mutableMapOf<PlatformType, Int>()

    // Tracks whether the current window state for a platform is known to be a non-feed modal
    // (such as a comment bottom sheet, share sheet, settings screen, or direct messaging window)
    private val isBlockedWindowByPlatform = mutableMapOf<PlatformType, Boolean>()

    override fun onCreate() {
        super.onCreate()
        val database = DoomlyDatabase.getInstance(applicationContext)
        preferences = DoomlyPreferences(applicationContext)
        notificationManager = MilestoneNotificationManager(applicationContext)
        repository = DoomlyRepository(
            dao = database.doomlyDao(),
            preferences = preferences,
            onMilestoneCrossed = { threshold, totalScrolls ->
                notificationManager.sendMilestoneNotification(threshold, totalScrolls)
            }
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkgName = event.packageName?.toString() ?: return
        val platform = PlatformType.fromPackageName(pkgName) ?: return

        if (!preferences.isPlatformTrackingEnabled(platform)) return

        val className = event.className?.toString().orEmpty()

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Check if the user opened a non-feed window (comments, settings, share sheet, inbox, keyboard)
                isBlockedWindowByPlatform[platform] = isKnownNonFeedWindow(className)
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (isBlockedWindowByPlatform[platform] == true) {
                    return
                }
                if (shouldCountAsShortVideoFeedScroll(platform, event, className)) {
                    val now = System.currentTimeMillis()
                    lastCountedScrollMsByPlatform[platform] = now
                    serviceScope.launch {
                        repository.recordEstimatedScroll(
                            platform = platform,
                            delta = 1,
                            timestampMs = now
                        )
                    }
                }
            }

            else -> Unit
        }
    }

    /**
     * Conservative heuristic to determine whether a `TYPE_VIEW_SCROLLED` event comes from a
     * vertical short-form video feed (Reels, Shorts, TikTok) rather than horizontal carousels,
     * tiny scroll jitters, or non-pager lists.
     *
     * If DOOMLY cannot confidently tell it is in a short-video feed, it does NOT count.
     */
    internal fun shouldCountAsShortVideoFeedScroll(
        platform: PlatformType,
        event: AccessibilityEvent,
        className: String
    ): Boolean {
        val now = System.currentTimeMillis()
        val lastScrollMs = lastCountedScrollMsByPlatform[platform] ?: 0L
        if (now - lastScrollMs < MIN_SWIPE_COOLDOWN_MS) {
            return false
        }

        // Ignore horizontal scroll events where horizontal delta dominates vertical delta
        val deltaX = kotlin.math.abs(event.scrollDeltaX)
        val deltaY = kotlin.math.abs(event.scrollDeltaY)
        if (deltaX > 0 && deltaX > deltaY) {
            return false
        }

        val normalizedClass = className.lowercase()

        // Exclude known non-feed scrollable widgets (horizontal scroll views, edit texts, menus, webviews)
        if (normalizedClass.contains("horizontalscroll") ||
            normalizedClass.contains("edittext") ||
            normalizedClass.contains("webview") ||
            normalizedClass.contains("tablayout") ||
            normalizedClass.contains("bottomsheet")
        ) {
            return false
        }

        // Short-form video feeds on Instagram Reels, YouTube Shorts, and TikTok use vertical
        // ViewPager / ViewPager2 / RecyclerView pager containers.
        val isPagerOrFeedContainer = normalizedClass.contains("viewpager") ||
            normalizedClass.contains("recyclerview") ||
            normalizedClass.contains("reel") ||
            normalizedClass.contains("short") ||
            normalizedClass.contains("verticalview")

        if (!isPagerOrFeedContainer) {
            return false
        }

        // Check index progression (typical for ViewPager2 / RecyclerView snapping 1 item per video)
        val fromIndex = event.fromIndex
        val itemCount = event.itemCount
        if (fromIndex >= 0 && itemCount > 1) {
            val previousIndex = lastFromIndexByPlatform[platform]
            lastFromIndexByPlatform[platform] = fromIndex
            if (previousIndex != null && fromIndex != previousIndex) {
                return true
            }
        }

        // Fallback for vertical pager scroll delta when index metadata is omitted by the host app
        if (deltaY >= MIN_VERTICAL_SCROLL_DELTA_PX) {
            return true
        }

        // Also check scrollY progression when maxScrollY indicates a full-screen vertical pager
        if (event.maxScrollY > 0 && event.scrollY > 0 && deltaX == 0) {
            return true
        }

        return false
    }

    private fun isKnownNonFeedWindow(className: String): Boolean {
        if (className.isBlank()) return false
        val lower = className.lowercase()
        return lower.contains("comment") ||
            lower.contains("bottomsheet") ||
            lower.contains("dialog") ||
            lower.contains("share") ||
            lower.contains("setting") ||
            lower.contains("direct") ||
            lower.contains("inbox") ||
            lower.contains("inputmethod")
    }

    override fun onInterrupt() {
        // No-op: DOOMLY does not hold continuous feedback resources.
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        // Minimum cooldown between counted video swipes to avoid counting one swipe's deceleration frames multiple times
        private const val MIN_SWIPE_COOLDOWN_MS = 650L
        private const val MIN_VERTICAL_SCROLL_DELTA_PX = 180
    }
}
