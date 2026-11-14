package com.example.tracking

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import java.time.LocalDate
import java.time.ZoneId

/**
 * Monitors real device state for DOOMLY:
 * 1. Installed status of Instagram, YouTube, and TikTok via PackageManager.
 * 2. Usage Access permission and real foreground App usage time via UsageStatsManager.
 * 3. Accessibility Service enabled state via Settings.Secure.
 */
class PlatformSystemMonitor(private val context: Context) {

    private val appContext = context.applicationContext

    /**
     * Checks whether any known package name for [platform] is actually installed on the device.
     * Never fakes installation state.
     */
    fun isPlatformInstalled(platform: PlatformType): Boolean {
        val pm = appContext.packageManager
        return platform.packageNames.any { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    /**
     * Checks if the user has granted Usage Access (PACKAGE_USAGE_STATS) in Android Settings.
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = appContext.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            appContext.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Checks if DoomlyAccessibilityService is currently enabled in Android Accessibility Settings.
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = ComponentName(appContext, DoomlyAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices
            .split(':')
            .mapNotNull { ComponentName.unflattenFromString(it) }
            .any { it == expectedComponent }
    }

    /**
     * Resolves the honest tracking status for [platform].
     */
    fun resolveTrackingStatus(
        platform: PlatformType,
        isScrollTrackingEnabledInPrefs: Boolean
    ): PlatformTrackingStatus {
        if (!isPlatformInstalled(platform)) {
            return PlatformTrackingStatus.NOT_INSTALLED
        }
        if (!isAccessibilityServiceEnabled()) {
            return PlatformTrackingStatus.PERMISSION_MISSING
        }
        if (!isScrollTrackingEnabledInPrefs) {
            return PlatformTrackingStatus.SCROLL_TRACKING_OFF
        }
        return PlatformTrackingStatus.ACTIVE
    }

    /**
     * Queries exact foreground app usage duration (in milliseconds) for [platform] on [date]
     * using UsageEvents (ACTIVITY_RESUMED / ACTIVITY_PAUSED / ACTIVITY_STOPPED) with fallback to
     * queryUsageStats. Returns null if Usage Access permission is not granted or app is not installed.
     */
    fun getAppUsageTimeMsForDate(
        platform: PlatformType,
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (!isPlatformInstalled(platform)) return null
        if (!hasUsageStatsPermission()) return null

        val usageStatsManager =
            appContext.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return 0L

        val startOfDayMs = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDayMs = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            .coerceAtMost(System.currentTimeMillis())

        if (endOfDayMs <= startOfDayMs) return 0L

        val targetPackages = platform.packageNames.toSet()

        // Calculate precise foreground intervals from UsageEvents within [startOfDayMs, endOfDayMs]
        var totalDurationMs = 0L
        val activeStartByPackage = mutableMapOf<String, Long>()

        try {
            val usageEvents = usageStatsManager.queryEvents(startOfDayMs, endOfDayMs)
            val event = UsageEvents.Event()
            while (usageEvents != null && usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                val pkg = event.packageName ?: continue
                if (pkg !in targetPackages) continue

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        activeStartByPackage[pkg] = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        val resumedAt = activeStartByPackage.remove(pkg)
                        if (resumedAt != null && event.timeStamp > resumedAt) {
                            totalDurationMs += (event.timeStamp - resumedAt)
                        }
                    }
                }
            }

            // If an activity is still in foreground right now on today's date, close the interval at endOfDayMs
            for ((_, resumedAt) in activeStartByPackage) {
                if (endOfDayMs > resumedAt) {
                    totalDurationMs += (endOfDayMs - resumedAt)
                }
            }

            // If UsageEvents was already purged by the OS for older dates (>7 days), fall back to aggregated daily stats
            if (totalDurationMs == 0L) {
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startOfDayMs,
                    endOfDayMs
                )
                if (!stats.isNullOrEmpty()) {
                    totalDurationMs = stats
                        .filter { it.packageName in targetPackages && it.firstTimeStamp >= startOfDayMs - 3_600_000L }
                        .sumOf { it.totalTimeInForeground }
                }
            }
        } catch (_: Exception) {
            return 0L
        }

        return totalDurationMs.coerceAtLeast(0L)
    }

    /**
     * Queries foreground usage time per date across a list of dates for 7-day / 30-day charts.
     */
    fun getUsageTimeHistoryMs(
        dates: List<LocalDate>,
        platforms: List<PlatformType> = PlatformType.entries,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Map<String, Map<PlatformType, Long>> {
        if (!hasUsageStatsPermission()) return emptyMap()
        val result = mutableMapOf<String, Map<PlatformType, Long>>()
        for (date in dates) {
            val perPlatform = mutableMapOf<PlatformType, Long>()
            for (platform in platforms) {
                val duration = getAppUsageTimeMsForDate(platform, date, zoneId) ?: 0L
                perPlatform[platform] = duration
            }
            result[date.toString()] = perPlatform
        }
        return result
    }
}
