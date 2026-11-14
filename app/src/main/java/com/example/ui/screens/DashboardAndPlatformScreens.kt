package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import com.example.ui.DaySummaryUiModel
import com.example.ui.DoomlyScreen
import com.example.ui.DoomlyUiState
import com.example.ui.PlatformDayUiModel
import com.example.ui.components.AnimatedEstimatedScrollsHero
import com.example.ui.components.BarChartEntry
import com.example.ui.components.BrainStatusDisclaimerCard
import com.example.ui.components.BrainStatusPill
import com.example.ui.components.DoomlyBarChart
import com.example.ui.components.DoomlyLogoMark
import com.example.ui.components.DoomlyMascot3D
import com.example.ui.components.DoomlyTopBar
import com.example.ui.components.EstimatedScrollsLabelWithInfo
import com.example.ui.components.TrackingStatusBadge
import com.example.ui.components.formatUsageDuration
import com.example.ui.theme.DoomlyBlack
import com.example.ui.theme.DoomlyBorder
import com.example.ui.theme.DoomlyCharcoal
import com.example.ui.theme.DoomlyDivider
import com.example.ui.theme.DoomlyMutedGray
import com.example.ui.theme.DoomlyRed
import com.example.ui.theme.DoomlyRedBorder
import com.example.ui.theme.DoomlyRedContainer
import com.example.ui.theme.DoomlySurfaceElevated
import com.example.ui.theme.DoomlyWhite

/**
 * Screen 4: Main Dashboard
 * Features:
 * - DOOMLY header + quick access to all secondary screens
 * - 3D Brain Mascot reacting to daily progress, counter-up animations, and tap interactions
 * - Animated Hero Combined Counter ("Estimated scrolls" + info icon, or "Tracking unavailable")
 * - Brain Status badge + non-medical disclaimer
 * - Today's Bar Graph comparing platforms
 * - Cards for Instagram Reels, YouTube Shorts, and TikTok
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    uiState: DoomlyUiState,
    onNavigate: (DoomlyScreen) -> Unit,
    onEnableScrollTracking: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("dashboard_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 580.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Brand Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DoomlyLogoMark(size = 42.dp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "DOOMLY",
                                style = MaterialTheme.typography.headlineSmall,
                                color = DoomlyWhite
                            )
                            Text(
                                text = "by BYREX",
                                style = MaterialTheme.typography.labelSmall,
                                color = DoomlyRed
                            )
                        }
                        Text(
                            text = "Today • ${uiState.todayDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DoomlyMutedGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dashboard_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh live tracking status",
                            tint = DoomlyMutedGray
                        )
                    }
                    IconButton(
                        onClick = { onNavigate(DoomlyScreen.SETTINGS) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Open Settings",
                            tint = DoomlyWhite
                        )
                    }
                }
            }

            // Loading / Error States
            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = DoomlyRed,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "Reading local usage and scroll records...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DoomlyWhite
                        )
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyRedContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyRed)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = DoomlyRed
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyWhite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Permission Missing Banner if Usage or Accessibility is off
            if (!uiState.hasUsageStatsPermission || !uiState.isAccessibilityServiceEnabled) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(DoomlyScreen.PERMISSIONS) }
                        .testTag("dashboard_permission_banner"),
                    color = DoomlySurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DoomlyRedBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    !uiState.hasUsageStatsPermission && !uiState.isAccessibilityServiceEnabled ->
                                        "Usage Access & Scroll Estimation permissions are off"
                                    !uiState.hasUsageStatsPermission ->
                                        "Usage Access permission is off (needed for App usage time)"
                                    else ->
                                        "Scroll Estimation permission is off (using App usage time only)"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = DoomlyWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap to view permission setup & privacy guarantee",
                                style = MaterialTheme.typography.bodySmall,
                                color = DoomlyMutedGray
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = DoomlyRed
                        )
                    }
                }
            }

            // Hero Card: 3D Mascot + Combined Estimated Scrolls + Brain Status + Total Usage Time
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DoomlyCharcoal,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DoomlyBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DoomlyMascot3D(
                        brainStatus = uiState.todayBrainStatus,
                        scrollCount = uiState.todayCombinedEstimatedScrolls,
                        mascotSize = 148.dp,
                        showSpeechBubbleOnTap = true,
                        initialSpeechVisible = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { onNavigate(DoomlyScreen.MASCOT_INTERACTION) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("open_mascot_interaction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SmartToy,
                            contentDescription = null,
                            tint = DoomlyRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open Mascot Interaction Area",
                            style = MaterialTheme.typography.labelLarge,
                            color = DoomlyWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    AnimatedEstimatedScrollsHero(
                        estimatedScrolls = uiState.todayCombinedEstimatedScrolls,
                        isAccessibilityEnabled = uiState.isAccessibilityServiceEnabled,
                        isMasterScrollTrackingEnabled = uiState.settings.masterScrollTrackingEnabled,
                        onEnableTrackingClick = onEnableScrollTracking
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BrainStatusPill(
                        brainStatus = uiState.todayBrainStatus,
                        onClick = { onNavigate(DoomlyScreen.BRAIN_STATUS) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.brain_disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        color = DoomlyMutedGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DoomlyDivider)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Combined App usage time row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.app_usage_time_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = DoomlyMutedGray
                            )
                            Text(
                                text = "Combined across installed short-video apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = DoomlyMutedGray
                            )
                        }
                        if (uiState.hasUsageStatsPermission) {
                            Text(
                                text = formatUsageDuration(uiState.todayCombinedUsageMs ?: 0L),
                                style = MaterialTheme.typography.headlineSmall,
                                color = DoomlyWhite,
                                modifier = Modifier.testTag("dashboard_combined_usage_time")
                            )
                        } else {
                            OutlinedButton(
                                onClick = { onNavigate(DoomlyScreen.PERMISSIONS) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DoomlyRedBorder),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("dashboard_enable_usage_button")
                            ) {
                                Text("Enable Usage Access", color = DoomlyRed)
                            }
                        }
                    }
                }
            }

            // Quick Navigation Grid for All Core Feature Screens
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                DashboardNavShortcut(
                    title = "Daily Statistics",
                    subtitle = "Pick any date",
                    icon = Icons.Outlined.CalendarMonth,
                    testTag = "nav_daily_stats_button",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(DoomlyScreen.DAILY_STATISTICS) }
                )
                DashboardNavShortcut(
                    title = "7 & 30-Day History",
                    subtitle = "Trends & bar charts",
                    icon = Icons.Outlined.BarChart,
                    testTag = "nav_history_button",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(DoomlyScreen.HISTORY) }
                )
                DashboardNavShortcut(
                    title = "Brain Status",
                    subtitle = uiState.todayBrainStatus.label,
                    icon = Icons.Outlined.Psychology,
                    testTag = "nav_brain_status_button",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(DoomlyScreen.BRAIN_STATUS) }
                )
                DashboardNavShortcut(
                    title = "Privacy & Data",
                    subtitle = "Permissions & reset",
                    icon = Icons.Outlined.Security,
                    testTag = "nav_privacy_info_button",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(DoomlyScreen.PRIVACY_INFO) }
                )
            }

            // Today's Bar Graph Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Platform Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        color = DoomlyWhite
                    )
                    EstimatedScrollsLabelWithInfo()
                }

                val installedPlatforms = PlatformType.entries.mapNotNull { uiState.todayPlatforms[it] }
                val barEntries = installedPlatforms.map { item ->
                    val scrollVal = (item.estimatedScrolls ?: 0).toFloat()
                    BarChartEntry(
                        label = when {
                            !item.isInstalled -> "${item.platform.shortName} (Absent)"
                            else -> item.platform.shortName
                        },
                        value = scrollVal,
                        displayValue = when {
                            !item.isInstalled -> "Not installed"
                            item.estimatedScrolls == null -> "Unavailable"
                            else -> "${item.estimatedScrolls}"
                        },
                        isHighlighted = scrollVal > 0f && scrollVal >= (installedPlatforms.maxOfOrNull { it.estimatedScrolls ?: 0 } ?: 0)
                    )
                }

                val emptyBarReason = when {
                    !uiState.isAccessibilityServiceEnabled ->
                        "Scroll estimation is off. Enable Accessibility permission to populate today's scroll bar graph."
                    !uiState.settings.masterScrollTrackingEnabled ->
                        "Scroll tracking is paused in Settings."
                    else ->
                        "No short-form feed scrolls estimated yet today."
                }

                DoomlyBarChart(
                    entries = barEntries,
                    emptyMessage = emptyBarReason,
                    modifier = Modifier.testTag("dashboard_today_bar_chart")
                )
            }

            // Platform Cards Section (Instagram Reels, YouTube Shorts, TikTok)
            Text(
                text = "Tracked Platforms",
                style = MaterialTheme.typography.titleMedium,
                color = DoomlyWhite
            )

            PlatformType.entries.forEach { platform ->
                val model = uiState.todayPlatforms[platform] ?: PlatformDayUiModel(
                    platform = platform,
                    isInstalled = false,
                    trackingStatus = PlatformTrackingStatus.NOT_INSTALLED,
                    estimatedScrolls = null,
                    usageDurationMs = null
                )
                DashboardPlatformCard(
                    model = model,
                    hasUsagePermission = uiState.hasUsageStatsPermission,
                    onClick = { onNavigate(DoomlyScreen.forPlatform(platform)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DashboardNavShortcut(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 68.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = DoomlyCharcoal,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DoomlyBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DoomlyRed,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DoomlyWhite
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = DoomlyMutedGray
                )
            }
        }
    }
}

@Composable
private fun DashboardPlatformCard(
    model: PlatformDayUiModel,
    hasUsagePermission: Boolean,
    onClick: () -> Unit
) {
    val tag = "platform_card_${model.platform.id.lowercase()}"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
        color = DoomlyCharcoal,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DoomlyBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.platform.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = DoomlyWhite
                    )
                    Text(
                        text = if (model.isInstalled) {
                            "Feed: ${model.platform.feedName}"
                        } else {
                            "App is not installed on this device"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = DoomlyMutedGray
                    )
                }
                TrackingStatusBadge(status = model.trackingStatus)
            }

            HorizontalDivider(color = DoomlyDivider)

            if (!model.isInstalled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.not_installed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DoomlyMutedGray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.labelMedium,
                            color = DoomlyWhite
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = DoomlyMutedGray
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Estimated scrolls column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.estimated_scrolls_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = DoomlyMutedGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (model.estimatedScrolls != null) {
                            val animatedCount by animateIntAsState(
                                targetValue = model.estimatedScrolls,
                                animationSpec = tween(350),
                                label = "platform_scroll_count"
                            )
                            Text(
                                text = animatedCount.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = DoomlyRed
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.tracking_unavailable),
                                style = MaterialTheme.typography.titleSmall,
                                color = DoomlyWhite
                            )
                        }
                    }

                    // App usage time column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.app_usage_time_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = DoomlyMutedGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (hasUsagePermission) {
                                formatUsageDuration(model.usageDurationMs ?: 0L)
                            } else {
                                stringResource(R.string.permission_missing)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = DoomlyWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open ${model.platform.displayName} details",
                        tint = DoomlyMutedGray
                    )
                }
            }
        }
    }
}

/**
 * Screens 5, 6, 7: Platform Detail Screen (Instagram Reels, YouTube Shorts, TikTok)
 * Each platform screen shows:
 * - Estimated scrolls today (with info icon, or "Tracking unavailable" + enable button, or "Not installed")
 * - App usage time
 * - Intraday & 7-day bar graph
 * - Daily history for this platform
 * - Clear tracking status (Not installed, Permission missing, Scroll tracking off, or Active)
 */
@Composable
fun PlatformDetailScreen(
    platform: PlatformType,
    uiState: DoomlyUiState,
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onTogglePlatformTracking: (Boolean) -> Unit
) {
    BackHandler(onBack = onBack)

    val model = uiState.todayPlatforms[platform] ?: PlatformDayUiModel(
        platform = platform,
        isInstalled = false,
        trackingStatus = PlatformTrackingStatus.NOT_INSTALLED,
        estimatedScrolls = null,
        usageDurationMs = null
    )

    val isEnabledInPrefs = uiState.settings.isPlatformTrackingEnabled(platform)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("platform_detail_${platform.id.lowercase()}")
    ) {
        DoomlyTopBar(
            title = platform.displayName,
            subtitle = "Short-form feed: ${platform.feedName}",
            onBack = onBack,
            trailingAction = {
                TrackingStatusBadge(status = model.trackingStatus)
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 580.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // State 1: App Not Installed (e.g. TikTok or Instagram not present on device)
                if (!model.isInstalled) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("platform_not_installed_card"),
                        color = DoomlyCharcoal,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, DoomlyBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TrackingStatusBadge(status = PlatformTrackingStatus.NOT_INSTALLED)
                            Text(
                                text = "${platform.displayName} is not installed",
                                style = MaterialTheme.typography.headlineSmall,
                                color = DoomlyWhite,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "DOOMLY checked your device via PackageManager (${platform.packageNames.joinToString()}) and confirmed ${platform.shortName} is not installed. DOOMLY never displays fake or placeholder statistics for absent apps.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DoomlyMutedGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Tracking Status Explanation Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (model.trackingStatus == PlatformTrackingStatus.ACTIVE) {
                            DoomlyCharcoal
                        } else {
                            DoomlySurfaceElevated
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (model.trackingStatus == PlatformTrackingStatus.ACTIVE) DoomlyBorder else DoomlyRedBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tracking Status",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DoomlyWhite
                                )
                                TrackingStatusBadge(status = model.trackingStatus)
                            }
                            Text(
                                text = when (model.trackingStatus) {
                                    PlatformTrackingStatus.ACTIVE ->
                                        "Active: DOOMLY is estimating vertical swipes inside ${platform.displayName} and measuring foreground App usage time."
                                    PlatformTrackingStatus.PERMISSION_MISSING ->
                                        "Permission missing: Optional Accessibility scroll estimation is off. Enable it to estimate ${platform.feedName} scrolls."
                                    PlatformTrackingStatus.SCROLL_TRACKING_OFF ->
                                        "Scroll tracking off: Scroll estimation for ${platform.shortName} is paused in DOOMLY settings."
                                    PlatformTrackingStatus.NOT_INSTALLED ->
                                        "${platform.shortName} is not installed."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = DoomlyMutedGray
                            )

                            if (model.trackingStatus == PlatformTrackingStatus.PERMISSION_MISSING) {
                                Button(
                                    onClick = onNavigateToPermissions,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DoomlyRed,
                                        contentColor = DoomlyWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("platform_enable_permission_button")
                                ) {
                                    Text("Enable Scroll Estimation Permission")
                                }
                            } else if (model.trackingStatus == PlatformTrackingStatus.SCROLL_TRACKING_OFF) {
                                Button(
                                    onClick = { onTogglePlatformTracking(true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DoomlyRed,
                                        contentColor = DoomlyWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("platform_resume_tracking_button")
                                ) {
                                    Text("Resume ${platform.shortName} Scroll Tracking")
                                }
                            }
                        }
                    }

                    // Primary Metrics Row: Estimated Scrolls Today + App Usage Time Today
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Estimated Scrolls Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = DoomlyCharcoal,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DoomlyBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                EstimatedScrollsLabelWithInfo()
                                if (model.estimatedScrolls != null) {
                                    val animatedScrolls by animateIntAsState(
                                        targetValue = model.estimatedScrolls,
                                        animationSpec = tween(400),
                                        label = "detail_scrolls"
                                    )
                                    Text(
                                        text = animatedScrolls.toString(),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = DoomlyRed,
                                        modifier = Modifier.testTag("platform_detail_scroll_count")
                                    )
                                    Text(
                                        text = "Today in ${platform.feedName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DoomlyMutedGray
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.tracking_unavailable),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DoomlyWhite,
                                        modifier = Modifier.testTag("platform_detail_scroll_unavailable")
                                    )
                                    Text(
                                        text = "Never shown as a fake 0",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DoomlyMutedGray
                                    )
                                }
                            }
                        }

                        // App Usage Time Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = DoomlyCharcoal,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DoomlyBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier.height(48.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = stringResource(R.string.app_usage_time_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = DoomlyMutedGray
                                    )
                                }
                                if (uiState.hasUsageStatsPermission) {
                                    Text(
                                        text = formatUsageDuration(model.usageDurationMs ?: 0L),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = DoomlyWhite,
                                        modifier = Modifier.testTag("platform_detail_usage_time")
                                    )
                                    Text(
                                        text = "Exact foreground time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DoomlyMutedGray
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.permission_missing),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DoomlyWhite
                                    )
                                    TextButton(
                                        onClick = onNavigateToPermissions,
                                        modifier = Modifier.testTag("platform_enable_usage_button")
                                    ) {
                                        Text("Enable Usage Access", color = DoomlyRed)
                                    }
                                }
                            }
                        }
                    }

                    // Today's 4-Hour Block / Hourly Bar Graph for this Platform
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Scroll Activity by Time of Day",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }

                        // Group 24 hours into 6 readable 4-hour blocks for clean mobile presentation
                        val blockLabels = listOf("0–4h", "4–8h", "8–12h", "12–16h", "16–20h", "20–24h")
                        val blockValues = (0 until 6).map { block ->
                            val startHour = block * 4
                            model.hourlyScrolls.subList(startHour, startHour + 4).sum()
                        }
                        val peakBlock = blockValues.maxOrNull() ?: 0
                        val intradayEntries = blockLabels.mapIndexed { idx, label ->
                            val v = blockValues[idx]
                            BarChartEntry(
                                label = label,
                                value = v.toFloat(),
                                displayValue = v.toString(),
                                isHighlighted = v > 0 && v == peakBlock
                            )
                        }

                        DoomlyBarChart(
                            entries = intradayEntries,
                            emptyMessage = if (model.trackingStatus == PlatformTrackingStatus.ACTIVE) {
                                "No estimated scrolls recorded for ${platform.displayName} yet today."
                            } else {
                                "Enable scroll tracking to view intraday ${platform.feedName} activity."
                            }
                        )
                    }

                    // 7-Day Platform Bar Graph
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Last 7 Days — ${platform.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )

                        val sevenDayPlatformEntries = uiState.history7Days.map { day ->
                            val count = day.perPlatformScrolls[platform] ?: 0
                            BarChartEntry(
                                label = day.shortLabel,
                                value = count.toFloat(),
                                displayValue = count.toString(),
                                isHighlighted = day.date == uiState.todayDate && count > 0
                            )
                        }

                        DoomlyBarChart(
                            entries = sevenDayPlatformEntries,
                            emptyMessage = "No recorded scroll history for ${platform.displayName} in the last 7 days."
                        )
                    }

                    // Daily History List for this Platform
                    Text(
                        text = "Daily History (${platform.shortName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DoomlyWhite
                    )

                    val platformHistoryRows = uiState.history30Days.reversed().filter { day ->
                        (day.perPlatformScrolls[platform] ?: 0) > 0 ||
                            (day.perPlatformUsageMs[platform] ?: 0L) > 0L ||
                            day.date == uiState.todayDate
                    }

                    if (platformHistoryRows.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = DoomlyCharcoal,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DoomlyBorder)
                        ) {
                            Text(
                                text = "No daily records yet for ${platform.displayName}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DoomlyMutedGray,
                                modifier = Modifier.padding(18.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            platformHistoryRows.take(14).forEach { day ->
                                PlatformHistoryRow(
                                    day = day,
                                    platform = platform,
                                    isToday = day.date == uiState.todayDate,
                                    hasUsagePermission = uiState.hasUsageStatsPermission,
                                    isTrackingActive = model.trackingStatus == PlatformTrackingStatus.ACTIVE
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PlatformHistoryRow(
    day: DaySummaryUiModel,
    platform: PlatformType,
    isToday: Boolean,
    hasUsagePermission: Boolean,
    isTrackingActive: Boolean
) {
    val scrolls = day.perPlatformScrolls[platform] ?: 0
    val usageMs = day.perPlatformUsageMs[platform] ?: 0L

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DoomlyCharcoal,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isToday) DoomlyRedBorder else DoomlyBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isToday) "${day.shortLabel} (Today)" else day.shortLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = DoomlyWhite
                )
                Text(
                    text = day.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = DoomlyMutedGray
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.estimated_scrolls_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = DoomlyMutedGray
                    )
                    Text(
                        text = if (scrolls > 0 || isTrackingActive) "$scrolls" else "Unavailable",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (scrolls > 0) DoomlyRed else DoomlyWhite
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.app_usage_time_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = DoomlyMutedGray
                    )
                    Text(
                        text = if (hasUsagePermission) formatUsageDuration(usageMs) else "No perm",
                        style = MaterialTheme.typography.titleMedium,
                        color = DoomlyWhite
                    )
                }
            }
        }
    }
}
