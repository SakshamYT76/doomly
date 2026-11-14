package com.example.ui

import com.example.data.BrainStatus
import com.example.data.DoomlySettingsState
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType

/**
 * All 14 screens in DOOMLY.
 */
enum class DoomlyScreen(val route: String, val title: String) {
    SPLASH("splash", "DOOMLY"),
    ONBOARDING("onboarding", "Welcome to DOOMLY"),
    PERMISSIONS("permissions", "Permissions & Honest Tracking"),
    DASHBOARD("dashboard", "DOOMLY"),
    PLATFORM_INSTAGRAM("platform_instagram", "Instagram Reels"),
    PLATFORM_YOUTUBE("platform_youtube", "YouTube Shorts"),
    PLATFORM_TIKTOK("platform_tiktok", "TikTok"),
    DAILY_STATISTICS("daily_statistics", "Daily Statistics"),
    HISTORY("history", "Scroll & Usage History"),
    BRAIN_STATUS("brain_status", "Brain Status"),
    MASCOT_INTERACTION("mascot_interaction", "Mascot Interaction"),
    SETTINGS("settings", "Settings"),
    PRIVACY_INFO("privacy_info", "Privacy & Permissions Info"),
    RESET_DATA("reset_data", "Reset & Data Management");

    companion object {
        fun forPlatform(platform: PlatformType): DoomlyScreen = when (platform) {
            PlatformType.INSTAGRAM -> PLATFORM_INSTAGRAM
            PlatformType.YOUTUBE -> PLATFORM_YOUTUBE
            PlatformType.TIKTOK -> PLATFORM_TIKTOK
        }
    }
}

/**
 * Per-platform snapshot for a given calendar date.
 */
data class PlatformDayUiModel(
    val platform: PlatformType,
    val isInstalled: Boolean,
    val trackingStatus: PlatformTrackingStatus,
    val estimatedScrolls: Int?, // null when not installed, permission missing, or scroll tracking off
    val usageDurationMs: Long?, // null when not installed or usage permission missing
    val hourlyScrolls: List<Int> = List(24) { 0 }
)

/**
 * Daily aggregate row for history charts and lists.
 */
data class DaySummaryUiModel(
    val date: String,
    val shortLabel: String,
    val combinedEstimatedScrolls: Int,
    val combinedUsageDurationMs: Long,
    val perPlatformScrolls: Map<PlatformType, Int>,
    val perPlatformUsageMs: Map<PlatformType, Long>,
    val brainStatus: BrainStatus
)

/**
 * Complete reactive UI state for DOOMLY.
 */
data class DoomlyUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val todayDate: String = "",
    val selectedDate: String = "",
    val settings: DoomlySettingsState = DoomlySettingsState(),
    val hasUsageStatsPermission: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val todayPlatforms: Map<PlatformType, PlatformDayUiModel> = emptyMap(),
    val selectedDatePlatforms: Map<PlatformType, PlatformDayUiModel> = emptyMap(),
    val todayCombinedEstimatedScrolls: Int? = null, // null if no installed platform has active scroll tracking
    val selectedDateCombinedScrolls: Int? = null,
    val todayCombinedUsageMs: Long? = null,
    val todayBrainStatus: BrainStatus = BrainStatus.FRESH_BRAIN,
    val selectedDateBrainStatus: BrainStatus = BrainStatus.FRESH_BRAIN,
    val todayMilestonesNotified: Set<Int> = emptySet(),
    val history7Days: List<DaySummaryUiModel> = emptyList(),
    val history30Days: List<DaySummaryUiModel> = emptyList(),
    val allRecordedDays: List<DaySummaryUiModel> = emptyList(),
    val statusBannerMessage: String? = null
)
