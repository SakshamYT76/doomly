package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import com.example.ui.DoomlyScreen
import com.example.ui.DoomlyUiState
import com.example.ui.components.DoomlyTopBar
import com.example.ui.components.TrackingStatusBadge
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
 * Screen 12: Settings Screen
 * Controls for scroll estimation toggles, milestone notification alerts, permission shortcuts,
 * and navigation to Privacy & Permissions Info and Reset & Data Management.
 */
@Composable
fun SettingsScreen(
    uiState: DoomlyUiState,
    onToggleMasterScrollTracking: (Boolean) -> Unit,
    onTogglePlatformScrollTracking: (PlatformType, Boolean) -> Unit,
    onToggleMilestoneNotifications: (Boolean) -> Unit,
    onNavigate: (DoomlyScreen) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("settings_screen")
    ) {
        DoomlyTopBar(
            title = "Settings",
            subtitle = "DOOMLY by BYREX • v1.0.0",
            onBack = onBack
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
                // Scroll Estimation Controls Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Scroll Estimation Controls",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )

                        SettingsSwitchRow(
                            title = "Master Scroll Estimation",
                            subtitle = "Enable or pause vertical swipe estimation across all platforms",
                            checked = uiState.settings.masterScrollTrackingEnabled,
                            onCheckedChange = onToggleMasterScrollTracking,
                            testTag = "switch_master_scroll_tracking"
                        )

                        HorizontalDivider(color = DoomlyDivider)

                        PlatformType.entries.forEach { platform ->
                            val platformModel = uiState.todayPlatforms[platform]
                            val installed = platformModel?.isInstalled == true
                            val checked = when (platform) {
                                PlatformType.INSTAGRAM -> uiState.settings.trackInstagram
                                PlatformType.YOUTUBE -> uiState.settings.trackYouTube
                                PlatformType.TIKTOK -> uiState.settings.trackTikTok
                            }

                            SettingsSwitchRow(
                                title = platform.displayName,
                                subtitle = if (installed) {
                                    "Track estimated scrolls in ${platform.feedName}"
                                } else {
                                    "Not installed on this device"
                                },
                                checked = checked && uiState.settings.masterScrollTrackingEnabled,
                                enabled = installed && uiState.settings.masterScrollTrackingEnabled,
                                onCheckedChange = { onTogglePlatformScrollTracking(platform, it) },
                                testTag = "switch_track_${platform.id.lowercase()}"
                            )
                        }
                    }
                }

                // Daily Milestone Notifications Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Daily Milestone Warnings",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )
                        SettingsSwitchRow(
                            title = "200 / 300 / 500 Scroll Alerts",
                            subtitle = "Send a one-time-per-day system notification when crossing each threshold",
                            checked = uiState.settings.milestoneNotificationsEnabled,
                            onCheckedChange = onToggleMilestoneNotifications,
                            testTag = "switch_milestone_notifications"
                        )
                    }
                }

                // Navigation Links to Permissions, Privacy Info, and Reset & Data Management
                Text(
                    text = "Permissions, Privacy & Local Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = DoomlyWhite
                )

                SettingsNavRow(
                    title = "Permission Setup & Live Status",
                    subtitle = "Usage Access, Accessibility Service, and Restricted Settings guide",
                    testTag = "settings_to_permissions_button",
                    onClick = { onNavigate(DoomlyScreen.PERMISSIONS) }
                )

                SettingsNavRow(
                    title = "Privacy & Permissions Info",
                    subtitle = "What is and isn't collected, zero-internet verification",
                    testTag = "settings_to_privacy_button",
                    onClick = { onNavigate(DoomlyScreen.PRIVACY_INFO) }
                )

                SettingsNavRow(
                    title = "Reset & Data Management",
                    subtitle = "Reset today's scroll count or delete all stored history",
                    testTag = "settings_to_reset_button",
                    onClick = { onNavigate(DoomlyScreen.RESET_DATA) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) DoomlyWhite else DoomlyMutedGray
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DoomlyMutedGray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = DoomlyWhite,
                checkedTrackColor = DoomlyRed,
                uncheckedThumbColor = DoomlyMutedGray,
                uncheckedTrackColor = DoomlySurfaceElevated
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = DoomlyCharcoal,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DoomlyBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DoomlyWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
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

/**
 * Screen 13: Privacy and Permissions Info Screen
 * Comprehensive explanation of DOOMLY's honest tracking, zero-internet architecture,
 * what is and isn't collected, and how to enable or revoke permissions.
 */
@Composable
fun PrivacyAndPermissionsInfoScreen(
    uiState: DoomlyUiState,
    onNavigateToPermissionsSetup: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("privacy_info_screen")
    ) {
        DoomlyTopBar(
            title = "Privacy & Permissions Info",
            subtitle = "100% local, transparent architecture",
            onBack = onBack
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
                // Zero Internet / Offline Guarantee Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyRedBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WifiOff,
                                contentDescription = null,
                                tint = DoomlyRed
                            )
                            Text(
                                text = "Zero Internet Access by Design",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }
                        Text(
                            text = "DOOMLY does not request android.permission.INTERNET in its AndroidManifest.xml. Because the Android OS blocks network sockets for apps without this permission, it is technically impossible for DOOMLY to upload, share, or transmit your data anywhere.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )
                    }
                }

                // What IS Collected vs What IS NEVER Collected
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = null,
                                tint = DoomlyRed
                            )
                            Text(
                                text = "What DOOMLY Saves vs. Never Reads",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }

                        Text(
                            text = "SAVED LOCALLY ON THIS PHONE:",
                            style = MaterialTheme.typography.labelLarge,
                            color = DoomlyWhite
                        )
                        Text(
                            text = "1. Daily and hourly estimated scroll counts per platform (Instagram, YouTube, TikTok).\n" +
                                "2. Timestamps of when scroll counts were updated and whether daily 200/300/500 warnings were shown.\n" +
                                "3. Local preferences (onboarding completed, tracking toggles).",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )

                        HorizontalDivider(color = DoomlyDivider)

                        Text(
                            text = "NEVER READ, STORED, OR TRANSMITTED:",
                            style = MaterialTheme.typography.labelLarge,
                            color = DoomlyRed
                        )
                        Text(
                            text = "• Screen text, captions, or video titles\n" +
                                "• Usernames, profiles, or creator names\n" +
                                "• Comments, direct messages, or keyboard input\n" +
                                "• Photos, videos, or audio\n\n" +
                                "DOOMLY sets android:canRetrieveWindowContent=\"false\" in its Accessibility Service configuration, so Android never grants DOOMLY access to screen text nodes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyWhite
                        )
                    }
                }

                // Why Scroll Counts are Labeled "Estimated Scrolls"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Why Scroll Counts Are Labeled 'Estimated scrolls'",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )
                        Text(
                            text = "Android provides an exact system API (UsageStatsManager) for measuring how long an app is in the foreground ('App usage time'). However, Android does not provide an API to count how many individual Reels, Shorts, or TikTok videos you watched.\n\n" +
                                "When you enable the optional Accessibility permission, DOOMLY observes vertical scroll events inside short-form video pager containers and filters out horizontal carousels, comment drawers, and rapid sub-second jitters. If DOOMLY cannot confidently tell it is in a short-video feed, it does not count. Because Instagram, YouTube, and TikTok periodically change their internal layout classes, this estimate can be slightly off.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )
                    }
                }

                // Live Permission Status & Sideloaded APK Restricted Settings Guide
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = DoomlyRed
                            )
                            Text(
                                text = "How to Enable Sideloaded Permissions",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }
                        Text(
                            text = "If you installed DOOMLY via an APK file on Android 13+, Android restricts Accessibility services by default until you explicitly allow restricted settings:\n\n" +
                                "1. Open Settings -> Accessibility -> Downloaded apps -> DOOMLY Scroll Estimator (tap the grayed-out toggle once).\n" +
                                "2. Open Settings -> Apps -> DOOMLY.\n" +
                                "3. Tap the 3-dot menu (⋮) in the top-right corner and select 'Allow restricted settings'.\n" +
                                "4. Go back to Accessibility Settings and enable 'DOOMLY Scroll Estimator'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToPermissionsSetup,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("privacy_to_permissions_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DoomlyRed,
                                    contentColor = DoomlyWhite
                                )
                            ) {
                                Text("Open Permission Setup")
                            }
                            OutlinedButton(
                                onClick = { openAppDetailsSettings(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DoomlyBorder)
                            ) {
                                Text("Open App Info", color = DoomlyWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Screen 14: Reset and Data Management Screen
 * Allows resetting today's scroll data or deleting all stored local data,
 * each protected by a clear confirmation dialog.
 */
@Composable
fun ResetAndDataManagementScreen(
    uiState: DoomlyUiState,
    onResetToday: () -> Unit,
    onDeleteAllData: () -> Unit,
    onClearBanner: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showResetTodayConfirmation by remember { mutableStateOf(false) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }

    if (showResetTodayConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetTodayConfirmation = false },
            containerColor = DoomlyCharcoal,
            titleContentColor = DoomlyWhite,
            textContentColor = DoomlyMutedGray,
            title = {
                Text("Reset Today's Data?", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    "This will reset today's (${uiState.todayDate}) estimated scroll counts, intraday hourly bars, and daily milestone notification flags back to zero. Previous days in your history will not be affected."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToday()
                        showResetTodayConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoomlyRed,
                        contentColor = DoomlyWhite
                    ),
                    modifier = Modifier.testTag("confirm_reset_today_button")
                ) {
                    Text("Reset Today")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetTodayConfirmation = false },
                    modifier = Modifier.testTag("cancel_reset_today_button")
                ) {
                    Text("Cancel", color = DoomlyMutedGray)
                }
            }
        )
    }

    if (showDeleteAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmation = false },
            containerColor = DoomlyCharcoal,
            titleContentColor = DoomlyWhite,
            textContentColor = DoomlyMutedGray,
            title = {
                Text("Delete All Stored Data?", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    "This will permanently delete all local estimated scroll history, hourly breakdowns, and milestone logs across all dates from this phone. Because DOOMLY has no cloud backup, this action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllData()
                        showDeleteAllConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoomlyRed,
                        contentColor = DoomlyWhite
                    ),
                    modifier = Modifier.testTag("confirm_delete_all_button")
                ) {
                    Text("Delete All Data")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllConfirmation = false },
                    modifier = Modifier.testTag("cancel_delete_all_button")
                ) {
                    Text("Cancel", color = DoomlyMutedGray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("reset_data_screen")
    ) {
        DoomlyTopBar(
            title = "Reset & Data Management",
            subtitle = "Manage your local SQLite records",
            onBack = onBack
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
                if (uiState.statusBannerMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_status_banner"),
                        color = DoomlyRedContainer,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DoomlyRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.statusBannerMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = DoomlyWhite,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onClearBanner) {
                                Text("Dismiss", color = DoomlyRed)
                            }
                        }
                    }
                }

                // Current Local Storage Summary
                val totalAllTimeScrolls = uiState.allRecordedDays.sumOf { it.combinedEstimatedScrolls }
                val activeRecordedDaysCount = uiState.allRecordedDays.count { it.combinedEstimatedScrolls > 0 }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Local Database Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Today's Estimated Scrolls:", color = DoomlyMutedGray, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${uiState.todayCombinedEstimatedScrolls ?: 0}",
                                color = DoomlyRed,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Days with Scroll Records:", color = DoomlyMutedGray, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "$activeRecordedDaysCount days",
                                color = DoomlyWhite,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("All-Time Estimated Scrolls:", color = DoomlyMutedGray, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "$totalAllTimeScrolls",
                                color = DoomlyWhite,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // Action 1: Reset Today
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RestartAlt,
                                contentDescription = null,
                                tint = DoomlyWhite
                            )
                            Text(
                                text = "Reset Today (${uiState.todayDate})",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }
                        Text(
                            text = "Clears today's estimated scroll counts, today's hourly bar chart, and today's milestone notification status while preserving past days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )
                        OutlinedButton(
                            onClick = { showResetTodayConfirmation = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 50.dp)
                                .testTag("reset_today_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DoomlyRedBorder)
                        ) {
                            Text("Reset Today's Data", color = DoomlyRed)
                        }
                    }
                }

                // Action 2: Delete All Data
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyRedBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteForever,
                                contentDescription = null,
                                tint = DoomlyRed
                            )
                            Text(
                                text = "Delete All Data",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }
                        Text(
                            text = "Permanently wipes every daily scroll record, hourly bar chart, and milestone log stored in DOOMLY's local SQLite database. Note: Android system App usage time is managed by Android OS Settings and is not modified.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )
                        Button(
                            onClick = { showDeleteAllConfirmation = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 50.dp)
                                .testTag("delete_all_data_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DoomlyRed,
                                contentColor = DoomlyWhite
                            )
                        ) {
                            Text("Delete All Local Data")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
