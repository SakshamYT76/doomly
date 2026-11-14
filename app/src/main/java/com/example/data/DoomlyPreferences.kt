package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DoomlySettingsState(
    val onboardingCompleted: Boolean = false,
    val masterScrollTrackingEnabled: Boolean = true,
    val trackInstagram: Boolean = true,
    val trackYouTube: Boolean = true,
    val trackTikTok: Boolean = true,
    val milestoneNotificationsEnabled: Boolean = true
) {
    fun isPlatformTrackingEnabled(platform: PlatformType): Boolean {
        if (!masterScrollTrackingEnabled) return false
        return when (platform) {
            PlatformType.INSTAGRAM -> trackInstagram
            PlatformType.YOUTUBE -> trackYouTube
            PlatformType.TIKTOK -> trackTikTok
        }
    }
}

class DoomlyPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<DoomlySettingsState> = _state.asStateFlow()

    private fun readState(): DoomlySettingsState {
        return DoomlySettingsState(
            onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false),
            masterScrollTrackingEnabled = prefs.getBoolean(KEY_MASTER_SCROLL_TRACKING, true),
            trackInstagram = prefs.getBoolean(KEY_TRACK_INSTAGRAM, true),
            trackYouTube = prefs.getBoolean(KEY_TRACK_YOUTUBE, true),
            trackTikTok = prefs.getBoolean(KEY_TRACK_TIKTOK, true),
            milestoneNotificationsEnabled = prefs.getBoolean(KEY_MILESTONE_NOTIFICATIONS, true)
        )
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _state.value = readState()
    }

    fun setMasterScrollTrackingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_SCROLL_TRACKING, enabled).apply()
        _state.value = readState()
    }

    fun setPlatformTrackingEnabled(platform: PlatformType, enabled: Boolean) {
        val key = when (platform) {
            PlatformType.INSTAGRAM -> KEY_TRACK_INSTAGRAM
            PlatformType.YOUTUBE -> KEY_TRACK_YOUTUBE
            PlatformType.TIKTOK -> KEY_TRACK_TIKTOK
        }
        prefs.edit().putBoolean(key, enabled).apply()
        _state.value = readState()
    }

    fun setMilestoneNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MILESTONE_NOTIFICATIONS, enabled).apply()
        _state.value = readState()
    }

    fun isPlatformTrackingEnabled(platform: PlatformType): Boolean {
        return readState().isPlatformTrackingEnabled(platform)
    }

    companion object {
        private const val PREFS_NAME = "doomly_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_MASTER_SCROLL_TRACKING = "master_scroll_tracking"
        private const val KEY_TRACK_INSTAGRAM = "track_instagram"
        private const val KEY_TRACK_YOUTUBE = "track_youtube"
        private const val KEY_TRACK_TIKTOK = "track_tiktok"
        private const val KEY_MILESTONE_NOTIFICATIONS = "milestone_notifications"
    }
}
