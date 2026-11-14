package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PlatformType
import com.example.ui.DoomlyScreen
import com.example.ui.DoomlyViewModel
import com.example.ui.screens.BrainStatusScreen
import com.example.ui.screens.DailyStatisticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MascotInteractionScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PermissionExplanationScreen
import com.example.ui.screens.PlatformDetailScreen
import com.example.ui.screens.PrivacyAndPermissionsInfoScreen
import com.example.ui.screens.ResetAndDataManagementScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DoomlyBlack
import com.example.ui.theme.DoomlyCharcoal
import com.example.ui.theme.DoomlyMutedGray
import com.example.ui.theme.DoomlyRed
import com.example.ui.theme.DoomlyRedContainer
import com.example.ui.theme.DoomlyWhite
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DoomlyViewModel by viewModels()

    private val dateRolloverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.checkMidnightRolloverAndRefresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(dateRolloverReceiver, filter)

        setContent {
            MyApplicationTheme {
                DoomlyApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh permissions and usage stats whenever user returns from Android Settings
        viewModel.checkMidnightRolloverAndRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(dateRolloverReceiver) }
    }
}

private data class BottomNavTab(
    val screen: DoomlyScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val bottomNavTabs = listOf(
    BottomNavTab(
        screen = DoomlyScreen.DASHBOARD,
        label = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "bottom_nav_dashboard"
    ),
    BottomNavTab(
        screen = DoomlyScreen.DAILY_STATISTICS,
        label = "Daily",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        testTag = "bottom_nav_daily"
    ),
    BottomNavTab(
        screen = DoomlyScreen.HISTORY,
        label = "History",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "bottom_nav_history"
    ),
    BottomNavTab(
        screen = DoomlyScreen.BRAIN_STATUS,
        label = "Brain",
        selectedIcon = Icons.Filled.Psychology,
        unselectedIcon = Icons.Outlined.Psychology,
        testTag = "bottom_nav_brain"
    ),
    BottomNavTab(
        screen = DoomlyScreen.SETTINGS,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "bottom_nav_settings"
    )
)

@Composable
fun DoomlyApp(viewModel: DoomlyViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navStack by viewModel.navigationStack.collectAsStateWithLifecycle()
    val currentScreen = navStack.lastOrNull() ?: DoomlyScreen.SPLASH

    val showBottomBar = currentScreen != DoomlyScreen.SPLASH &&
        currentScreen != DoomlyScreen.ONBOARDING &&
        !(currentScreen == DoomlyScreen.PERMISSIONS && navStack.size == 1)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DoomlyBlack,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DoomlyCharcoal,
                    tonalElevation = 0.dp
                ) {
                    bottomNavTabs.forEach { tab ->
                        val isSelected = currentScreen == tab.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(tab.screen) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DoomlyRed,
                                selectedTextColor = DoomlyWhite,
                                indicatorColor = DoomlyRedContainer,
                                unselectedIconColor = DoomlyMutedGray,
                                unselectedTextColor = DoomlyMutedGray
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 14 })
                        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -it / 14 })
                },
                label = "doomly_screen_transition"
            ) { screen ->
                when (screen) {
                    DoomlyScreen.SPLASH -> {
                        SplashScreen(
                            onFinished = { viewModel.completeSplash() }
                        )
                    }

                    DoomlyScreen.ONBOARDING -> {
                        OnboardingScreen(
                            onComplete = { viewModel.completeOnboarding() }
                        )
                    }

                    DoomlyScreen.PERMISSIONS -> {
                        PermissionExplanationScreen(
                            hasUsagePermission = uiState.hasUsageStatsPermission,
                            isAccessibilityEnabled = uiState.isAccessibilityServiceEnabled,
                            hasNotificationPermission = uiState.hasNotificationPermission,
                            showBackButton = navStack.size > 1,
                            onBack = { viewModel.navigateBack() },
                            onContinueToDashboard = { viewModel.finishPermissionSetupToDashboard() },
                            onRefreshStatus = { viewModel.checkMidnightRolloverAndRefresh() }
                        )
                    }

                    DoomlyScreen.DASHBOARD -> {
                        DashboardScreen(
                            uiState = uiState,
                            onNavigate = { target -> viewModel.navigateTo(target) },
                            onEnableScrollTracking = {
                                if (!uiState.isAccessibilityServiceEnabled) {
                                    viewModel.navigateTo(DoomlyScreen.PERMISSIONS)
                                } else {
                                    viewModel.setMasterScrollTracking(true)
                                }
                            },
                            onRefresh = { viewModel.checkMidnightRolloverAndRefresh() }
                        )
                    }

                    DoomlyScreen.PLATFORM_INSTAGRAM -> {
                        PlatformDetailScreen(
                            platform = PlatformType.INSTAGRAM,
                            uiState = uiState,
                            onBack = { viewModel.navigateBack() },
                            onNavigateToPermissions = { viewModel.navigateTo(DoomlyScreen.PERMISSIONS) },
                            onTogglePlatformTracking = { enabled ->
                                viewModel.setMasterScrollTracking(true)
                                viewModel.setPlatformScrollTracking(PlatformType.INSTAGRAM, enabled)
                            }
                        )
                    }

                    DoomlyScreen.PLATFORM_YOUTUBE -> {
                        PlatformDetailScreen(
                            platform = PlatformType.YOUTUBE,
                            uiState = uiState,
                            onBack = { viewModel.navigateBack() },
                            onNavigateToPermissions = { viewModel.navigateTo(DoomlyScreen.PERMISSIONS) },
                            onTogglePlatformTracking = { enabled ->
                                viewModel.setMasterScrollTracking(true)
                                viewModel.setPlatformScrollTracking(PlatformType.YOUTUBE, enabled)
                            }
                        )
                    }

                    DoomlyScreen.PLATFORM_TIKTOK -> {
                        PlatformDetailScreen(
                            platform = PlatformType.TIKTOK,
                            uiState = uiState,
                            onBack = { viewModel.navigateBack() },
                            onNavigateToPermissions = { viewModel.navigateTo(DoomlyScreen.PERMISSIONS) },
                            onTogglePlatformTracking = { enabled ->
                                viewModel.setMasterScrollTracking(true)
                                viewModel.setPlatformScrollTracking(PlatformType.TIKTOK, enabled)
                            }
                        )
                    }

                    DoomlyScreen.DAILY_STATISTICS -> {
                        DailyStatisticsScreen(
                            uiState = uiState,
                            onSelectDate = { date -> viewModel.selectDate(date) },
                            onNavigateToPermissions = { viewModel.navigateTo(DoomlyScreen.PERMISSIONS) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    DoomlyScreen.HISTORY -> {
                        HistoryScreen(
                            uiState = uiState,
                            onSelectDateAndInspect = { date ->
                                viewModel.selectDate(date)
                                viewModel.navigateTo(DoomlyScreen.DAILY_STATISTICS)
                            },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    DoomlyScreen.BRAIN_STATUS -> {
                        BrainStatusScreen(
                            uiState = uiState,
                            onNavigateToMascotArea = { viewModel.navigateTo(DoomlyScreen.MASCOT_INTERACTION) },
                            onNavigateToPermissions = { viewModel.navigateTo(DoomlyScreen.PERMISSIONS) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    DoomlyScreen.MASCOT_INTERACTION -> {
                        MascotInteractionScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    DoomlyScreen.SETTINGS -> {
                        SettingsScreen(
                            uiState = uiState,
                            onToggleMasterScrollTracking = { viewModel.setMasterScrollTracking(it) },
                            onTogglePlatformScrollTracking = { p, enabled ->
                                viewModel.setPlatformScrollTracking(p, enabled)
                            },
                            onToggleMilestoneNotifications = { viewModel.setMilestoneNotificationsEnabled(it) },
                            onNavigate = { target -> viewModel.navigateTo(target) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    DoomlyScreen.PRIVACY_INFO -> {
                        PrivacyAndPermissionsInfoScreen(
                            uiState = uiState,
                            onNavigateToPermissionsSetup = { viewModel.navigateTo(DoomlyScreen.PERMISSIONS) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    DoomlyScreen.RESET_DATA -> {
                        ResetAndDataManagementScreen(
                            uiState = uiState,
                            onResetToday = { viewModel.resetTodayData() },
                            onDeleteAllData = { viewModel.deleteAllData() },
                            onClearBanner = { viewModel.clearStatusBanner() },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
