package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrainStatus
import com.example.data.DailyScrollEntity
import com.example.data.DoomlyDatabase
import com.example.data.DoomlyPreferences
import com.example.data.DoomlyRepository
import com.example.data.HourlyScrollEntity
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import com.example.tracking.MilestoneNotificationManager
import com.example.tracking.PlatformSystemMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DoomlyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DoomlyDatabase.getInstance(application)
    private val preferences = DoomlyPreferences(application)
    private val notificationManager = MilestoneNotificationManager(application)
    private val systemMonitor = PlatformSystemMonitor(application)
    private val repository = DoomlyRepository(
        dao = database.doomlyDao(),
        preferences = preferences,
        onMilestoneCrossed = { threshold, totalScrolls ->
            notificationManager.sendMilestoneNotification(threshold, totalScrolls)
        }
    )

    private val _todayDate = MutableStateFlow(DoomlyRepository.todayDateString())
    private val _selectedDate = MutableStateFlow(DoomlyRepository.todayDateString())
    private val _refreshTrigger = MutableStateFlow(0L)

    private val _navigationStack = MutableStateFlow(listOf(DoomlyScreen.SPLASH))
    val navigationStack: StateFlow<List<DoomlyScreen>> = _navigationStack.asStateFlow()

    private val _uiState = MutableStateFlow(
        DoomlyUiState(
            isLoading = true,
            todayDate = _todayDate.value,
            selectedDate = _selectedDate.value,
            settings = preferences.state.value
        )
    )
    val uiState: StateFlow<DoomlyUiState> = _uiState.asStateFlow()

    init {
        observeDataStreams()
        startMidnightAndSystemWatcher()
    }

    private fun observeDataStreams() {
        viewModelScope.launch {
            val todayFlow = _todayDate.flatMapLatest { date ->
                combine(
                    repository.observeDailyScrollsForDate(date),
                    repository.observeHourlyScrollsForDate(date),
                    repository.observeMilestonesForDate(date)
                ) { daily, hourly, milestones ->
                    Triple(daily, hourly, milestones.map { it.threshold }.toSet())
                }
            }

            val selectedDateFlow = _selectedDate.flatMapLatest { date ->
                combine(
                    repository.observeDailyScrollsForDate(date),
                    repository.observeHourlyScrollsForDate(date)
                ) { daily, hourly ->
                    Pair(daily, hourly)
                }
            }

            combine(
                todayFlow,
                selectedDateFlow,
                repository.observeAllDailyScrolls(),
                preferences.state,
                _refreshTrigger
            ) { todayTriple, selectedPair, allScrolls, settingsState, _ ->
                 Quintuple(todayTriple, selectedPair, allScrolls, settingsState, _todayDate.value)
            }.collect { (todayTriple, selectedPair, allScrolls, settingsState, todayStr) ->
                try {
                    val selectedStr = _selectedDate.value
                    val computed = withContext(Dispatchers.IO) {
                        buildUiState(
                            todayStr = todayStr,
                            selectedStr = selectedStr,
                            todayDaily = todayTriple.first,
                            todayHourly = todayTriple.second,
                            todayMilestones = todayTriple.third,
                            selectedDaily = selectedPair.first,
                            selectedHourly = selectedPair.second,
                            allScrolls = allScrolls,
                            settings = settingsState
                        )
                    }
                    _uiState.value = computed
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to load local stats: ${e.localizedMessage ?: "Unknown error"}"
                        )
                    }
                }
            }
        }
    }

    private fun buildUiState(
        todayStr: String,
        selectedStr: String,
        todayDaily: List<DailyScrollEntity>,
        todayHourly: List<HourlyScrollEntity>,
        todayMilestones: Set<Int>,
        selectedDaily: List<DailyScrollEntity>,
        selectedHourly: List<HourlyScrollEntity>,
        allScrolls: List<DailyScrollEntity>,
        settings: com.example.data.DoomlySettingsState
    ): DoomlyUiState {
        val hasUsagePerm = systemMonitor.hasUsageStatsPermission()
        val hasAccessibilityPerm = systemMonitor.isAccessibilityServiceEnabled()
        val hasNotificationPerm = notificationManager.hasNotificationPermission()

        val todayLocalDate = runCatching { LocalDate.parse(todayStr) }.getOrDefault(LocalDate.now())
        val selectedLocalDate = runCatching { LocalDate.parse(selectedStr) }.getOrDefault(todayLocalDate)

        val todayPlatforms = buildPlatformMapForDate(
            date = todayLocalDate,
            dailyEntities = todayDaily,
            hourlyEntities = todayHourly,
            settings = settings,
            hasUsagePerm = hasUsagePerm,
            hasAccessibilityPerm = hasAccessibilityPerm
        )

        val selectedPlatforms = buildPlatformMapForDate(
            date = selectedLocalDate,
            dailyEntities = selectedDaily,
            hourlyEntities = selectedHourly,
            settings = settings,
            hasUsagePerm = hasUsagePerm,
            hasAccessibilityPerm = hasAccessibilityPerm
        )

        // Honest combined estimated scrolls:
        // Only show a number if Accessibility permission is ON and at least one installed platform has scroll tracking active
        val anyActiveTrackingToday = todayPlatforms.values.any {
            it.trackingStatus == PlatformTrackingStatus.ACTIVE
        }
        val todayCombinedScrolls: Int? = if (anyActiveTrackingToday) {
            todayPlatforms.values
                .filter { it.isInstalled && it.estimatedScrolls != null }
                .sumOf { it.estimatedScrolls ?: 0 }
        } else {
            null
        }

        val anyActiveOrRecordedSelected = selectedPlatforms.values.any {
            it.trackingStatus == PlatformTrackingStatus.ACTIVE || (it.estimatedScrolls ?: 0) > 0
        }
        val selectedCombinedScrolls: Int? = if (anyActiveOrRecordedSelected) {
            selectedPlatforms.values
                .filter { it.isInstalled && it.estimatedScrolls != null }
                .sumOf { it.estimatedScrolls ?: 0 }
        } else {
            null
        }

        val todayCombinedUsageMs: Long? = if (hasUsagePerm) {
            todayPlatforms.values
                .filter { it.isInstalled }
                .sumOf { it.usageDurationMs ?: 0L }
        } else {
            null
        }

        // Build 7-day and 30-day history summaries
        val dates30 = (29 downTo 0).map { todayLocalDate.minusDays(it.toLong()) }
        val usageByDateMap = systemMonitor.getUsageTimeHistoryMs(dates30)
        val scrollsByDateMap = allScrolls.groupBy { it.date }

        val history30 = dates30.map { d ->
            val dStr = d.format(DoomlyRepository.DATE_FORMATTER)
            val dayScrollEntities = scrollsByDateMap[dStr].orEmpty()
            val perPlatformScrolls = PlatformType.entries.associateWith { p ->
                dayScrollEntities.find { it.platform == p.id }?.estimatedScrolls ?: 0
            }
            val perPlatformUsage = usageByDateMap[dStr] ?: PlatformType.entries.associateWith { 0L }
            val totalScrolls = perPlatformScrolls.values.sum()
            val totalUsage = perPlatformUsage.values.sum()
            DaySummaryUiModel(
                date = dStr,
                shortLabel = d.format(SHORT_DAY_FORMATTER),
                combinedEstimatedScrolls = totalScrolls,
                combinedUsageDurationMs = totalUsage,
                perPlatformScrolls = perPlatformScrolls,
                perPlatformUsageMs = perPlatformUsage,
                brainStatus = BrainStatus.fromScrollCount(totalScrolls)
            )
        }

        val history7 = history30.takeLast(7)

        val allRecordedDates = scrollsByDateMap.keys
            .union(dates30.map { it.format(DoomlyRepository.DATE_FORMATTER) })
            .sortedDescending()

        val allRecordedSummaries = allRecordedDates.mapNotNull { dStr ->
            val parsed = runCatching { LocalDate.parse(dStr) }.getOrNull() ?: return@mapNotNull null
            val dayScrollEntities = scrollsByDateMap[dStr].orEmpty()
            val perPlatformScrolls = PlatformType.entries.associateWith { p ->
                dayScrollEntities.find { it.platform == p.id }?.estimatedScrolls ?: 0
            }
            val perPlatformUsage = usageByDateMap[dStr] ?: PlatformType.entries.associateWith {
                systemMonitor.getAppUsageTimeMsForDate(it, parsed) ?: 0L
            }
            val totalScrolls = perPlatformScrolls.values.sum()
            val totalUsage = perPlatformUsage.values.sum()
            DaySummaryUiModel(
                date = dStr,
                shortLabel = parsed.format(SHORT_DAY_FORMATTER),
                combinedEstimatedScrolls = totalScrolls,
                combinedUsageDurationMs = totalUsage,
                perPlatformScrolls = perPlatformScrolls,
                perPlatformUsageMs = perPlatformUsage,
                brainStatus = BrainStatus.fromScrollCount(totalScrolls)
            )
        }

        val effectiveTodayScrollsForBrain = todayCombinedScrolls ?: 0
        val effectiveSelectedScrollsForBrain = selectedCombinedScrolls ?: 0

        return DoomlyUiState(
            isLoading = false,
            errorMessage = null,
            todayDate = todayStr,
            selectedDate = selectedStr,
            settings = settings,
            hasUsageStatsPermission = hasUsagePerm,
            isAccessibilityServiceEnabled = hasAccessibilityPerm,
            hasNotificationPermission = hasNotificationPerm,
            todayPlatforms = todayPlatforms,
            selectedDatePlatforms = selectedPlatforms,
            todayCombinedEstimatedScrolls = todayCombinedScrolls,
            selectedDateCombinedScrolls = selectedCombinedScrolls,
            todayCombinedUsageMs = todayCombinedUsageMs,
            todayBrainStatus = BrainStatus.fromScrollCount(effectiveTodayScrollsForBrain),
            selectedDateBrainStatus = BrainStatus.fromScrollCount(effectiveSelectedScrollsForBrain),
            todayMilestonesNotified = todayMilestones,
            history7Days = history7,
            history30Days = history30,
            allRecordedDays = allRecordedSummaries,
            statusBannerMessage = _uiState.value.statusBannerMessage
        )
    }

    private fun buildPlatformMapForDate(
        date: LocalDate,
        dailyEntities: List<DailyScrollEntity>,
        hourlyEntities: List<HourlyScrollEntity>,
        settings: com.example.data.DoomlySettingsState,
        hasUsagePerm: Boolean,
        hasAccessibilityPerm: Boolean
    ): Map<PlatformType, PlatformDayUiModel> {
        return PlatformType.entries.associateWith { platform ->
            val installed = systemMonitor.isPlatformInstalled(platform)
            val trackingEnabledInPrefs = settings.isPlatformTrackingEnabled(platform)
            val status = when {
                !installed -> PlatformTrackingStatus.NOT_INSTALLED
                !hasAccessibilityPerm -> PlatformTrackingStatus.PERMISSION_MISSING
                !trackingEnabledInPrefs -> PlatformTrackingStatus.SCROLL_TRACKING_OFF
                else -> PlatformTrackingStatus.ACTIVE
            }

            val recordedCount = dailyEntities.find { it.platform == platform.id }?.estimatedScrolls
            // Honest display: never show a fake 0 when tracking is unavailable and nothing was recorded
            val estimatedScrolls: Int? = when {
                !installed -> null
                status == PlatformTrackingStatus.ACTIVE -> recordedCount ?: 0
                recordedCount != null && recordedCount > 0 -> recordedCount
                else -> null
            }

            val usageMs: Long? = if (installed && hasUsagePerm) {
                systemMonitor.getAppUsageTimeMsForDate(platform, date)
            } else {
                null
            }

            val platformHourly = hourlyEntities.filter { it.platform == platform.id }
            val hourlyBuckets = List(24) { hour ->
                platformHourly.find { it.hour == hour }?.estimatedScrolls ?: 0
            }

            PlatformDayUiModel(
                platform = platform,
                isInstalled = installed,
                trackingStatus = status,
                estimatedScrolls = estimatedScrolls,
                usageDurationMs = usageMs,
                hourlyScrolls = hourlyBuckets
            )
        }
    }

    private fun startMidnightAndSystemWatcher() {
        viewModelScope.launch {
            while (isActive) {
                delay(15_000L)
                checkMidnightRolloverAndRefresh()
            }
        }
    }

    /**
     * Checks if the local calendar date rolled over past midnight ("00:00") and refreshes
     * system permissions & usage stats.
     */
    fun checkMidnightRolloverAndRefresh() {
        val currentToday = DoomlyRepository.todayDateString()
        val previousToday = _todayDate.value
        if (currentToday != previousToday) {
            _todayDate.value = currentToday
            if (_selectedDate.value == previousToday) {
                _selectedDate.value = currentToday
            }
        }
        _refreshTrigger.value = System.currentTimeMillis()
    }

    // Navigation actions
    fun completeSplash() {
        val nextScreen = if (preferences.state.value.onboardingCompleted) {
            DoomlyScreen.DASHBOARD
        } else {
            DoomlyScreen.ONBOARDING
        }
        _navigationStack.value = listOf(nextScreen)
    }

    fun completeOnboarding() {
        preferences.setOnboardingCompleted(true)
        _navigationStack.value = listOf(DoomlyScreen.PERMISSIONS)
    }

    fun finishPermissionSetupToDashboard() {
        preferences.setOnboardingCompleted(true)
        _navigationStack.value = listOf(DoomlyScreen.DASHBOARD)
    }

    fun navigateTo(screen: DoomlyScreen) {
        _navigationStack.update { stack ->
            if (stack.lastOrNull() == screen) stack else stack + screen
        }
    }

    fun navigateBack(): Boolean {
        val current = _navigationStack.value
        return if (current.size > 1) {
            _navigationStack.value = current.dropLast(1)
            true
        } else if (current.firstOrNull() != DoomlyScreen.DASHBOARD &&
            current.firstOrNull() != DoomlyScreen.SPLASH &&
            current.firstOrNull() != DoomlyScreen.ONBOARDING
        ) {
            _navigationStack.value = listOf(DoomlyScreen.DASHBOARD)
            true
        } else {
            false
        }
    }

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
        _refreshTrigger.value = System.currentTimeMillis()
    }

    // Settings mutations
    fun setMasterScrollTracking(enabled: Boolean) {
        preferences.setMasterScrollTrackingEnabled(enabled)
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setPlatformScrollTracking(platform: PlatformType, enabled: Boolean) {
        preferences.setPlatformTrackingEnabled(platform, enabled)
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setMilestoneNotificationsEnabled(enabled: Boolean) {
        preferences.setMilestoneNotificationsEnabled(enabled)
        _refreshTrigger.value = System.currentTimeMillis()
    }

    // Data reset mutations
    fun resetTodayData() {
        viewModelScope.launch {
            val today = _todayDate.value
            repository.resetDateData(today)
            _uiState.update { it.copy(statusBannerMessage = "Today's scroll data has been reset.") }
            _refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
            _uiState.update { it.copy(statusBannerMessage = "All local DOOMLY scroll history has been permanently deleted.") }
            _refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

    companion object {
        private val SHORT_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
    }
}
