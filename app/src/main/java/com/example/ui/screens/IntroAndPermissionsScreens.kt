package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.BrainStatus
import com.example.ui.components.DoomlyLogoMark
import com.example.ui.components.DoomlyMascot3D
import com.example.ui.components.DoomlyTopBar
import com.example.ui.theme.DoomlyBlack
import com.example.ui.theme.DoomlyBorder
import com.example.ui.theme.DoomlyCharcoal
import com.example.ui.theme.DoomlyMutedGray
import com.example.ui.theme.DoomlyRed
import com.example.ui.theme.DoomlyRedBorder
import com.example.ui.theme.DoomlyRedContainer
import com.example.ui.theme.DoomlySurfaceElevated
import com.example.ui.theme.DoomlyWhite
import kotlinx.coroutines.delay

/**
 * Screen 1: Splash Screen
 * Displays DOOMLY logo, "by BYREX" brand signature, and auto-advances or allows instant tap to proceed.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
        delay(1100L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .padding(24.dp)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DoomlyLogoMark(size = 96.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "DOOMLY",
                style = MaterialTheme.typography.displayMedium,
                color = DoomlyWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "by BYREX",
                style = MaterialTheme.typography.labelLarge,
                color = DoomlyRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Honest short-form scroll awareness.\n100% local. Zero cloud.",
                style = MaterialTheme.typography.bodyMedium,
                color = DoomlyMutedGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(36.dp))
            TextButton(
                onClick = onFinished,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("splash_continue_button")
            ) {
                Text(
                    text = "Enter DOOMLY",
                    style = MaterialTheme.typography.labelLarge,
                    color = DoomlyWhite
                )
            }
        }
    }
}

/**
 * Screen 2: Onboarding Screen (Short, shown once)
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val steps = listOf(
        OnboardingPageData(
            tag = "SHORT-FORM AWARENESS",
            title = "See how much you really scroll.",
            body = "DOOMLY tracks daily activity across Instagram Reels, YouTube Shorts, and TikTok (if installed) so you can break unconscious scrolling loops.",
            highlights = listOf(
                "Tracks Instagram Reels, YouTube Shorts, and TikTok",
                "Playful 3D Brain Mascot reacts to your daily scroll volume",
                "Optional daily warning alerts at 200, 300, and 500 scrolls"
            )
        ),
        OnboardingPageData(
            tag = "HONEST MEASUREMENT",
            title = "Usage time vs. Estimated scrolls.",
            body = "Android measures exact App usage time, but cannot directly count individual Reels or Shorts. DOOMLY estimates scrolls by counting vertical swipes inside short-video feeds.",
            highlights = listOf(
                "App usage time is measured accurately via Android Usage Stats",
                "Scroll counts are clearly labeled 'Estimated scrolls' everywhere",
                "If scroll permission is off, DOOMLY still works with usage time only"
            )
        ),
        OnboardingPageData(
            tag = "100% LOCAL & PRIVATE",
            title = "Your screen stays yours.",
            body = "DOOMLY has no login, no cloud, no ads, and does not even request internet permission. Everything stays in a local database on your phone.",
            highlights = listOf(
                "Never reads, stores, or sends text, usernames, comments, or messages",
                "Only anonymous scroll counts and timestamps are saved locally",
                "Reset today's count or delete all data anytime in two taps"
            )
        )
    )

    val page = steps[currentStep]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DoomlyLogoMark(size = 36.dp)
                    Column {
                        Text(
                            text = "DOOMLY",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )
                        Text(
                            text = "by BYREX",
                            style = MaterialTheme.typography.labelSmall,
                            color = DoomlyRed
                        )
                    }
                }
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("onboarding_skip_button")
                ) {
                    Text("Skip", color = DoomlyMutedGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DoomlyMascot3D(
                brainStatus = when (currentStep) {
                    0 -> BrainStatus.FRESH_BRAIN
                    1 -> BrainStatus.A_BIT_BAD
                    else -> BrainStatus.FRESH_BRAIN
                },
                scrollCount = null,
                mascotSize = 132.dp,
                showSpeechBubbleOnTap = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = DoomlyRedContainer,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, DoomlyRedBorder)
            ) {
                Text(
                    text = page.tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = DoomlyRed,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge,
                color = DoomlyWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyLarge,
                color = DoomlyMutedGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                    page.highlights.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(DoomlyRed)
                            )
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DoomlyWhite
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (index == currentStep) 26.dp else 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (index == currentStep) DoomlyRed else DoomlyBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 52.dp)
                            .testTag("onboarding_back_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DoomlyBorder)
                    ) {
                        Text("Back", color = DoomlyWhite)
                    }
                }
                Button(
                    onClick = {
                        if (currentStep < steps.lastIndex) {
                            currentStep += 1
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoomlyRed,
                        contentColor = DoomlyWhite
                    )
                ) {
                    Text(
                        text = if (currentStep < steps.lastIndex) "Next" else "Review Permissions",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

private data class OnboardingPageData(
    val tag: String,
    val title: String,
    val body: String,
    val highlights: List<String>
)

/**
 * Screen 3: Permission Explanation Screen
 * Shows clear in-app explanation of what is and isn't collected BEFORE sending the user to
 * any system permission screen, live permission status, sideloaded "Allow restricted settings"
 * instructions, and an option to continue with limited tracking.
 */
@Composable
fun PermissionExplanationScreen(
    hasUsagePermission: Boolean,
    isAccessibilityEnabled: Boolean,
    hasNotificationPermission: Boolean,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onContinueToDashboard: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    val context = LocalContext.current
    var showRestrictedSettingsGuide by remember { mutableStateOf(true) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshStatus()
    }

    if (showBackButton) {
        BackHandler(onBack = onBack)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("permission_explanation_screen")
    ) {
        if (showBackButton) {
            DoomlyTopBar(
                title = "Permissions & Setup",
                subtitle = "Live status and transparent data guarantees",
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!showBackButton) {
                    Text(
                        text = "Honest Tracking Setup",
                        style = MaterialTheme.typography.headlineLarge,
                        color = DoomlyWhite
                    )
                    Text(
                        text = "Before enabling any permission, review exactly what DOOMLY collects and what it never touches.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DoomlyMutedGray
                    )
                }

                // Clear In-App Pre-Permission Disclosure Card
                Surface(
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = DoomlyRed
                            )
                            Text(
                                text = "What Is & Isn't Collected",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }
                        Text(
                            text = "• COLLECTED LOCALLY: Foreground app usage duration (minutes) and vertical scroll gesture counts with timestamps for Instagram, YouTube, and TikTok.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyWhite
                        )
                        Text(
                            text = "• NEVER COLLECTED: DOOMLY never reads, stores, or sends screen text, video titles, usernames, comments, messages, or passwords. Accessibility window content retrieval (canRetrieveWindowContent) is disabled at the OS level.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )
                        Text(
                            text = "• 100% OFFLINE: DOOMLY does not request internet permission. Your data never leaves this phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )
                    }
                }

                // Step 1: Usage Access
                PermissionStepCard(
                    stepNumber = "STEP 1",
                    title = "App Usage Time (Usage Access)",
                    isGranted = hasUsagePermission,
                    icon = Icons.Outlined.QueryStats,
                    whatItDoes = "Allows Android's UsageStatsManager to report accurate foreground time spent in Instagram, YouTube, and TikTok.",
                    whenOff = "If off, App usage time shows 'Permission missing'.",
                    buttonText = if (hasUsagePermission) "Usage Access Enabled" else "Open Usage Access Settings",
                    buttonTestTag = "grant_usage_access_button",
                    onActionClick = {
                        openUsageAccessSettings(context)
                    }
                )

                // Step 2: Accessibility Service for Estimated Scrolls
                PermissionStepCard(
                    stepNumber = "STEP 2 (OPTIONAL)",
                    title = "Estimated Scrolls (Accessibility Service)",
                    isGranted = isAccessibilityEnabled,
                    icon = Icons.Outlined.AccessibilityNew,
                    whatItDoes = "Observes vertical swipe gestures inside short-form video feeds to calculate 'Estimated scrolls'. Only counts and timestamps are saved.",
                    whenOff = "If off, DOOMLY still works with App usage time, and scroll counts honestly show 'Tracking unavailable' (never a fake 0).",
                    buttonText = if (isAccessibilityEnabled) "Scroll Estimation Active" else "Open Accessibility Settings",
                    buttonTestTag = "grant_accessibility_button",
                    onActionClick = {
                        openAccessibilitySettings(context)
                    }
                )

                // Android 13+ Sideloaded APK "Allow Restricted Settings" Guide
                Surface(
                    color = DoomlySurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyRedBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = DoomlyRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Sideloaded APK? 'Restricted Setting' Fix",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DoomlyWhite
                                )
                            }
                            TextButton(
                                onClick = { showRestrictedSettingsGuide = !showRestrictedSettingsGuide },
                                modifier = Modifier.testTag("toggle_restricted_guide_button")
                            ) {
                                Text(
                                    text = if (showRestrictedSettingsGuide) "Hide" else "Show Steps",
                                    color = DoomlyRed
                                )
                            }
                        }

                        if (showRestrictedSettingsGuide) {
                            Text(
                                text = "On Android 13 and newer, if you installed DOOMLY directly from an APK file, Android blocks Accessibility until you allow restricted settings once:",
                                style = MaterialTheme.typography.bodySmall,
                                color = DoomlyMutedGray
                            )
                            Text(
                                text = "1. Tap 'Open Accessibility Settings' above and tap 'DOOMLY Scroll Estimator' once so Android sees the request.\n" +
                                    "2. Tap 'Open DOOMLY App Info' below.\n" +
                                    "3. Tap the 3-dot menu (⋮) in the top-right corner of the App Info screen.\n" +
                                    "4. Tap 'Allow restricted settings' and confirm with your PIN/biometrics.\n" +
                                    "5. Return to Accessibility Settings and turn on 'DOOMLY Scroll Estimator'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DoomlyWhite
                            )
                            OutlinedButton(
                                onClick = { openAppDetailsSettings(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("open_app_info_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DoomlyBorder)
                            ) {
                                Text("Open DOOMLY App Info (for ⋮ Menu)", color = DoomlyWhite)
                            }
                        }
                    }
                }

                // Step 3: Notifications for 200 / 300 / 500 Milestones
                PermissionStepCard(
                    stepNumber = "STEP 3 (OPTIONAL)",
                    title = "Daily Milestone Warnings (Notifications)",
                    isGranted = hasNotificationPermission,
                    icon = Icons.Outlined.Notifications,
                    whatItDoes = "Sends a one-time-per-day system notification when your combined daily estimated scrolls hit 200, 300, and 500.",
                    whenOff = "If off, milestone status is still visible inside the Brain Status screen.",
                    buttonText = if (hasNotificationPermission) "Notifications Enabled" else "Allow Milestone Notifications",
                    buttonTestTag = "grant_notifications_button",
                    onActionClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openAppDetailsSettings(context)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onRefreshStatus()
                        onContinueToDashboard()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 54.dp)
                        .testTag("continue_to_dashboard_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoomlyRed,
                        contentColor = DoomlyWhite
                    )
                ) {
                    val bothGranted = hasUsagePermission && isAccessibilityEnabled
                    Text(
                        text = if (bothGranted) {
                            "Continue to Dashboard"
                        } else {
                            "Continue with Limited Tracking"
                        },
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PermissionStepCard(
    stepNumber: String,
    title: String,
    isGranted: Boolean,
    icon: ImageVector,
    whatItDoes: String,
    whenOff: String,
    buttonText: String,
    buttonTestTag: String,
    onActionClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DoomlyCharcoal,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isGranted) DoomlyRedBorder else DoomlyBorder)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) DoomlyRed else DoomlyWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stepNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = DoomlyMutedGray
                    )
                }
                Surface(
                    color = if (isGranted) DoomlyRedContainer else DoomlySurfaceElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (isGranted) DoomlyRed else DoomlyBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isGranted) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = DoomlyRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isGranted) "Active" else "Not enabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = DoomlyWhite
                        )
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DoomlyWhite
            )

            Text(
                text = whatItDoes,
                style = MaterialTheme.typography.bodySmall,
                color = DoomlyWhite
            )

            Text(
                text = whenOff,
                style = MaterialTheme.typography.bodySmall,
                color = DoomlyMutedGray
            )

            if (isGranted) {
                OutlinedButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag(buttonTestTag),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Text(buttonText, color = DoomlyMutedGray)
                }
            } else {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag(buttonTestTag),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoomlyRed,
                        contentColor = DoomlyWhite
                    )
                ) {
                    Text(buttonText, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

fun openUsageAccessSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallback = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {
            openAppDetailsSettings(context)
        }
    }
}

fun openAccessibilitySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        openAppDetailsSettings(context)
    }
}

fun openAppDetailsSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Ignore if unavailable in restricted test environment
    }
}
