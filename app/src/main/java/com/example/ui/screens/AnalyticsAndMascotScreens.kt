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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BrainStatus
import com.example.data.DoomlyRepository
import com.example.data.PlatformTrackingStatus
import com.example.data.PlatformType
import com.example.ui.DoomlyUiState
import com.example.ui.components.BarChartEntry
import com.example.ui.components.BrainStatusDisclaimerCard
import com.example.ui.components.BrainStatusPill
import com.example.ui.components.DoomlyBarChart
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Screen 8: Daily Statistics Screen (Pick any date)
 * Allows inspecting combined & per-platform Estimated scrolls, App usage time,
 * intraday breakdown bar chart, and Brain Status for any calendar date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStatisticsScreen(
    uiState: DoomlyUiState,
    onSelectDate: (String) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showDatePickerModal by remember { mutableStateOf(false) }

    val selectedLocalDate = remember(uiState.selectedDate) {
        runCatching { LocalDate.parse(uiState.selectedDate) }.getOrDefault(LocalDate.now())
    }
    val todayLocalDate = remember(uiState.todayDate) {
        runCatching { LocalDate.parse(uiState.todayDate) }.getOrDefault(LocalDate.now())
    }

    if (showDatePickerModal) {
        val initialMillis = selectedLocalDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onSelectDate(picked.format(DoomlyRepository.DATE_FORMATTER))
                        }
                        showDatePickerModal = false
                    },
                    modifier = Modifier.testTag("confirm_date_picker_button")
                ) {
                    Text("Select Date", color = DoomlyRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerModal = false }) {
                    Text("Cancel", color = DoomlyMutedGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("daily_statistics_screen")
    ) {
        DoomlyTopBar(
            title = "Daily Statistics",
            subtitle = "Inspect any calendar date",
            onBack = onBack,
            trailingAction = {
                OutlinedButton(
                    onClick = { showDatePickerModal = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DoomlyRedBorder),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("open_date_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = DoomlyRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pick Date", color = DoomlyWhite)
                }
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
                // Date Selector Bar (Previous Day / Current Date / Next Day / Today Shortcut)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val prev = selectedLocalDate.minusDays(1)
                                onSelectDate(prev.format(DoomlyRepository.DATE_FORMATTER))
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("prev_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous day",
                                tint = DoomlyWhite
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { showDatePickerModal = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (uiState.selectedDate == uiState.todayDate) {
                                    "${uiState.selectedDate} (Today)"
                                } else {
                                    uiState.selectedDate
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite,
                                modifier = Modifier.testTag("selected_date_label")
                            )
                            Text(
                                text = "Tap to open calendar",
                                style = MaterialTheme.typography.labelSmall,
                                color = DoomlyRed
                            )
                        }

                        val canGoForward = selectedLocalDate.isBefore(todayLocalDate)
                        IconButton(
                            onClick = {
                                if (canGoForward) {
                                    val next = selectedLocalDate.plusDays(1)
                                    onSelectDate(next.format(DoomlyRepository.DATE_FORMATTER))
                                }
                            },
                            enabled = canGoForward,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("next_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next day",
                                tint = if (canGoForward) DoomlyWhite else DoomlyBorder
                            )
                        }
                    }
                }

                if (uiState.selectedDate != uiState.todayDate) {
                    TextButton(
                        onClick = { onSelectDate(uiState.todayDate) },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("jump_to_today_button")
                    ) {
                        Text("Return to Today (${uiState.todayDate})", color = DoomlyRed)
                    }
                }

                // Summary Card for Selected Date
                val selectedPlatforms = uiState.selectedDatePlatforms.values.toList()
                val totalUsageForSelected = selectedPlatforms
                    .filter { it.isInstalled }
                    .sumOf { it.usageDurationMs ?: 0L }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EstimatedScrollsLabelWithInfo()

                        if (uiState.selectedDateCombinedScrolls != null) {
                            Text(
                                text = uiState.selectedDateCombinedScrolls.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                color = DoomlyRed,
                                modifier = Modifier.testTag("daily_stats_combined_scrolls")
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.tracking_unavailable),
                                style = MaterialTheme.typography.headlineSmall,
                                color = DoomlyWhite,
                                modifier = Modifier.testTag("daily_stats_tracking_unavailable")
                            )
                            Text(
                                text = "Scroll tracking was off or no scroll estimate was recorded on this date.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DoomlyMutedGray,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onNavigateToPermissions,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DoomlyRed,
                                    contentColor = DoomlyWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("Enable Scroll Estimation")
                            }
                        }

                        BrainStatusPill(brainStatus = uiState.selectedDateBrainStatus)

                        HorizontalDivider(color = DoomlyDivider)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.app_usage_time_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = DoomlyMutedGray
                            )
                            Text(
                                text = if (uiState.hasUsageStatsPermission) {
                                    formatUsageDuration(totalUsageForSelected)
                                } else {
                                    stringResource(R.string.permission_missing)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = DoomlyWhite
                            )
                        }
                    }
                }

                // Intraday 4-hour block bar chart for selected date
                Text(
                    text = "Hourly Activity Breakdown (${uiState.selectedDate})",
                    style = MaterialTheme.typography.titleMedium,
                    color = DoomlyWhite
                )

                val combinedHourly = List(24) { hour ->
                    selectedPlatforms.sumOf { it.hourlyScrolls.getOrElse(hour) { 0 } }
                }
                val blockLabels = listOf("0–4h", "4–8h", "8–12h", "12–16h", "16–20h", "20–24h")
                val blockSums = (0 until 6).map { b ->
                    combinedHourly.subList(b * 4, b * 4 + 4).sum()
                }
                val maxBlock = blockSums.maxOrNull() ?: 0
                val blockEntries = blockLabels.mapIndexed { i, label ->
                    val v = blockSums[i]
                    BarChartEntry(
                        label = label,
                        value = v.toFloat(),
                        displayValue = v.toString(),
                        isHighlighted = v > 0 && v == maxBlock
                    )
                }

                DoomlyBarChart(
                    entries = blockEntries,
                    emptyMessage = "No estimated scrolls recorded on ${uiState.selectedDate}."
                )

                // Per-platform breakdown for selected date
                Text(
                    text = "Platform Breakdown (${uiState.selectedDate})",
                    style = MaterialTheme.typography.titleMedium,
                    color = DoomlyWhite
                )

                PlatformType.entries.forEach { platform ->
                    val pModel = uiState.selectedDatePlatforms[platform]
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = platform.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DoomlyWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TrackingStatusBadge(
                                    status = pModel?.trackingStatus ?: PlatformTrackingStatus.NOT_INSTALLED
                                )
                            }

                            if (pModel == null || !pModel.isInstalled) {
                                Text(
                                    text = stringResource(R.string.not_installed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DoomlyMutedGray
                                )
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.estimated_scrolls_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DoomlyMutedGray
                                        )
                                        Text(
                                            text = pModel.estimatedScrolls?.toString()
                                                ?: stringResource(R.string.tracking_unavailable),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if ((pModel.estimatedScrolls ?: 0) > 0) DoomlyRed else DoomlyWhite
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.app_usage_time_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DoomlyMutedGray
                                        )
                                        Text(
                                            text = if (uiState.hasUsageStatsPermission) {
                                                formatUsageDuration(pModel.usageDurationMs ?: 0L)
                                            } else {
                                                stringResource(R.string.permission_missing)
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = DoomlyWhite
                                        )
                                    }
                                }
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
 * Screen 9: History Screen with 7-day and 30-day bar graphs
 */
@Composable
fun HistoryScreen(
    uiState: DoomlyUiState,
    onSelectDateAndInspect: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedWindowDays by remember { mutableIntStateOf(7) }
    val activeDays = if (selectedWindowDays == 7) uiState.history7Days else uiState.history30Days

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("history_screen")
    ) {
        DoomlyTopBar(
            title = "Scroll & Usage History",
            subtitle = "7-day and 30-day local history",
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
                // 7-Day vs 30-Day Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val is7Day = selectedWindowDays == 7
                    Button(
                        onClick = { selectedWindowDays = 7 },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("history_7day_tab"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (is7Day) DoomlyRed else DoomlyCharcoal,
                            contentColor = DoomlyWhite
                        )
                    ) {
                        Text("Last 7 Days")
                    }
                    Button(
                        onClick = { selectedWindowDays = 30 },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("history_30day_tab"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!is7Day) DoomlyRed else DoomlyCharcoal,
                            contentColor = DoomlyWhite
                        )
                    ) {
                        Text("Last 30 Days")
                    }
                }

                // Summary Cards (Total Scrolls, Daily Average, Total Usage Time)
                val totalScrollsInWindow = activeDays.sumOf { it.combinedEstimatedScrolls }
                val avgScrollsInWindow = if (activeDays.isNotEmpty()) {
                    totalScrollsInWindow / activeDays.size
                } else 0
                val totalUsageMsInWindow = activeDays.sumOf { it.combinedUsageDurationMs }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = DoomlyCharcoal,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DoomlyBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${selectedWindowDays}d Est. Scrolls",
                                style = MaterialTheme.typography.labelSmall,
                                color = DoomlyMutedGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalScrollsInWindow",
                                style = MaterialTheme.typography.headlineMedium,
                                color = DoomlyRed
                            )
                            Text(
                                text = "Avg $avgScrollsInWindow / day",
                                style = MaterialTheme.typography.labelSmall,
                                color = DoomlyMutedGray
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = DoomlyCharcoal,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DoomlyBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${selectedWindowDays}d Usage Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = DoomlyMutedGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.hasUsageStatsPermission) {
                                    formatUsageDuration(totalUsageMsInWindow)
                                } else {
                                    stringResource(R.string.permission_missing)
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                color = DoomlyWhite
                            )
                            Text(
                                text = "Exact foreground time",
                                style = MaterialTheme.typography.labelSmall,
                                color = DoomlyMutedGray
                            )
                        }
                    }
                }

                // Estimated Scrolls Bar Graph (7-day or 30-day)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedWindowDays-Day Estimated Scrolls",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )
                        EstimatedScrollsLabelWithInfo()
                    }

                    val peakScrollDay = activeDays.maxOfOrNull { it.combinedEstimatedScrolls } ?: 0
                    val scrollBarEntries = activeDays.map { day ->
                        BarChartEntry(
                            label = day.shortLabel,
                            value = day.combinedEstimatedScrolls.toFloat(),
                            displayValue = day.combinedEstimatedScrolls.toString(),
                            isHighlighted = day.combinedEstimatedScrolls > 0 &&
                                day.combinedEstimatedScrolls == peakScrollDay
                        )
                    }

                    DoomlyBarChart(
                        entries = scrollBarEntries,
                        emptyMessage = "No estimated scrolls recorded in the last $selectedWindowDays days.",
                        showValuesAboveBars = selectedWindowDays == 7,
                        modifier = Modifier.testTag("history_scrolls_bar_chart")
                    )
                }

                // App Usage Time Bar Graph (7-day or 30-day)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "$selectedWindowDays-Day App Usage Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = DoomlyWhite
                    )

                    val peakUsageDay = activeDays.maxOfOrNull { it.combinedUsageDurationMs } ?: 0L
                    val usageBarEntries = activeDays.map { day ->
                        val minutes = (day.combinedUsageDurationMs / 60_000f)
                        BarChartEntry(
                            label = day.shortLabel,
                            value = minutes,
                            displayValue = formatUsageDuration(day.combinedUsageDurationMs),
                            isHighlighted = day.combinedUsageDurationMs > 0L &&
                                day.combinedUsageDurationMs == peakUsageDay
                        )
                    }

                    DoomlyBarChart(
                        entries = usageBarEntries,
                        emptyMessage = if (uiState.hasUsageStatsPermission) {
                            "No foreground usage time recorded for Instagram, YouTube, or TikTok in the last $selectedWindowDays days."
                        } else {
                            "Grant Usage Access permission to view your $selectedWindowDays-day App usage time bar graph."
                        },
                        showValuesAboveBars = selectedWindowDays == 7,
                        modifier = Modifier.testTag("history_usage_bar_chart")
                    )
                }

                // Daily Breakdown List
                Text(
                    text = "Daily Breakdown (Tap any day to inspect)",
                    style = MaterialTheme.typography.titleMedium,
                    color = DoomlyWhite
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeDays.reversed().forEach { day ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDateAndInspect(day.date) },
                            color = DoomlyCharcoal,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (day.date == uiState.todayDate) DoomlyRedBorder else DoomlyBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (day.date == uiState.todayDate) {
                                            "${day.shortLabel} (Today)"
                                        } else {
                                            day.shortLabel
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DoomlyWhite
                                    )
                                    Text(
                                        text = "${day.date} • ${day.brainStatus.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DoomlyMutedGray
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.estimated_scrolls_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DoomlyMutedGray
                                        )
                                        Text(
                                            text = "${day.combinedEstimatedScrolls}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (day.combinedEstimatedScrolls > 0) DoomlyRed else DoomlyWhite
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = stringResource(R.string.app_usage_time_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DoomlyMutedGray
                                        )
                                        Text(
                                            text = if (uiState.hasUsageStatsPermission) {
                                                formatUsageDuration(day.combinedUsageDurationMs)
                                            } else {
                                                "No perm"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = DoomlyWhite
                                        )
                                    }
                                }
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
 * Screen 10: Brain Status Screen (Playful, Not Medical)
 * Shows:
 * - 3D Brain Mascot reacting to current daily status
 * - Combined daily estimated scrolls
 * - All 4 Brain Status tiers:
 *   0–99: Fresh Brain
 *   100–199: A Bit Bad
 *   200–299: Worse
 *   300+: Bad
 * - Daily milestone warning indicators (200, 300, 500 scrolls)
 * - Mandatory disclaimer: "These labels are playful awareness indicators, not medical or scientific assessments."
 */
@Composable
fun BrainStatusScreen(
    uiState: DoomlyUiState,
    onNavigateToMascotArea: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val currentStatus = uiState.todayBrainStatus
    val currentScrolls = uiState.todayCombinedEstimatedScrolls

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("brain_status_screen")
    ) {
        DoomlyTopBar(
            title = "Brain Status",
            subtitle = "Playful daily scroll awareness",
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
                // Mandatory Playful Awareness Disclaimer at Top
                BrainStatusDisclaimerCard(modifier = Modifier.testTag("brain_status_disclaimer"))

                // Hero Mascot + Current Status Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DoomlyMascot3D(
                            brainStatus = currentStatus,
                            scrollCount = currentScrolls,
                            mascotSize = 160.dp,
                            showSpeechBubbleOnTap = true,
                            initialSpeechVisible = true
                        )

                        BrainStatusPill(brainStatus = currentStatus)

                        Text(
                            text = currentStatus.headline,
                            style = MaterialTheme.typography.titleLarge,
                            color = DoomlyWhite,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = currentStatus.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DoomlyMutedGray,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(color = DoomlyDivider)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EstimatedScrollsLabelWithInfo()
                            if (currentScrolls != null) {
                                Text(
                                    text = "$currentScrolls today",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = DoomlyRed
                                )
                            } else {
                                TextButton(onClick = onNavigateToPermissions) {
                                    Text(stringResource(R.string.tracking_unavailable), color = DoomlyRed)
                                }
                            }
                        }

                        // Progress toward 300+ threshold
                        val progress = ((currentScrolls ?: 0) / 300f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = DoomlyRed,
                            trackColor = DoomlySurfaceElevated
                        )

                        OutlinedButton(
                            onClick = onNavigateToMascotArea,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("brain_status_to_mascot_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DoomlyBorder)
                        ) {
                            Text("Open Mascot Interaction Area", color = DoomlyWhite)
                        }
                    }
                }

                // All 4 Brain Status Tiers
                Text(
                    text = "Brain Status Scale",
                    style = MaterialTheme.typography.titleMedium,
                    color = DoomlyWhite
                )

                BrainStatus.entries.forEach { tier ->
                    val isCurrentTier = tier == currentStatus && currentScrolls != null
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isCurrentTier) DoomlyRedContainer else DoomlyCharcoal,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isCurrentTier) DoomlyRed else DoomlyBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = tier.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DoomlyWhite
                                    )
                                    if (isCurrentTier) {
                                        Surface(
                                            color = DoomlyRed,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = "CURRENT",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = DoomlyWhite,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tier.headline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DoomlyMutedGray
                                )
                            }
                            Text(
                                text = tier.rangeText,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isCurrentTier) DoomlyRed else DoomlyWhite
                            )
                        }
                    }
                }

                // One-Time-Per-Day Milestone Warning Thresholds (200, 300, 500)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = DoomlyRed
                            )
                            Text(
                                text = "Daily Warning Milestones (Once Per Day)",
                                style = MaterialTheme.typography.titleMedium,
                                color = DoomlyWhite
                            )
                        }
                        Text(
                            text = "DOOMLY sends a one-time-per-day system notification when your combined daily estimated scrolls reach 200, 300, and 500.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BrainStatus.MILESTONE_THRESHOLDS.forEach { threshold ->
                                val reached = uiState.todayMilestonesNotified.contains(threshold) ||
                                    (currentScrolls != null && currentScrolls >= threshold)
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = if (reached) DoomlyRedContainer else DoomlySurfaceElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (reached) DoomlyRed else DoomlyBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "$threshold",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = if (reached) DoomlyRed else DoomlyWhite
                                        )
                                        Text(
                                            text = if (reached) "Reached today" else "Not reached",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DoomlyMutedGray
                                        )
                                    }
                                }
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
 * Screen 11: Mascot Interaction Area
 * Interactive stage for DOOMLY's 3D Brain Mascot ("Cortex"):
 * - Reacts to user taps with physics spring bounce and playful awareness quotes
 * - Demonstrates the counter-increment pulse animation
 * - Allows inspecting the mascot's expressions across all 4 Brain Status moods while
 *   clearly distinguishing the user's real live daily status.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MascotInteractionScreen(
    uiState: DoomlyUiState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var previewMoodOverride by remember { mutableStateOf<BrainStatus?>(null) }
    var animationDemoCounter by remember { mutableIntStateOf(uiState.todayCombinedEstimatedScrolls ?: 0) }
    var tapCount by remember { mutableIntStateOf(0) }

    val displayedMood = previewMoodOverride ?: uiState.todayBrainStatus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoomlyBlack)
            .testTag("mascot_interaction_screen")
    ) {
        DoomlyTopBar(
            title = "Mascot Interaction Area",
            subtitle = "Interactive 3D Brain Mascot",
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DoomlyCharcoal,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, DoomlyBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (previewMoodOverride == null) {
                                    "Live Mood: ${displayedMood.label}"
                                } else {
                                    "Previewing Mood: ${displayedMood.label}"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = DoomlyWhite
                            )
                            Text(
                                text = "Taps: $tapCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = DoomlyRed
                            )
                        }

                        DoomlyMascot3D(
                            brainStatus = displayedMood,
                            scrollCount = animationDemoCounter,
                            mascotSize = 200.dp,
                            showSpeechBubbleOnTap = true,
                            initialSpeechVisible = true,
                            onMascotTapped = {
                                tapCount += 1
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    animationDemoCounter += 1
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("mascot_counter_pulse_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DoomlyRed,
                                    contentColor = DoomlyWhite
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TouchApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Preview Scroll Pulse")
                            }

                            if (previewMoodOverride != null) {
                                OutlinedButton(
                                    onClick = { previewMoodOverride = null },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("mascot_reset_live_mood_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DoomlyBorder)
                                ) {
                                    Text("Use Live Mood", color = DoomlyWhite)
                                }
                            }
                        }
                    }
                }

                // Mood State Inspector (Preview how the 3D mascot reacts across all 4 tiers)
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
                            text = "Inspect Mascot Mood Expressions",
                            style = MaterialTheme.typography.titleMedium,
                            color = DoomlyWhite
                        )
                        Text(
                            text = "Your real live Brain Status is '${uiState.todayBrainStatus.label}'. Select any tier below to preview how the mascot looks and responds at that scroll level:",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoomlyMutedGray
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 2
                        ) {
                            BrainStatus.entries.forEach { mood ->
                                val isSelected = displayedMood == mood
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 52.dp)
                                        .clickable { previewMoodOverride = mood }
                                        .testTag("preview_mood_${mood.name.lowercase()}"),
                                    color = if (isSelected) DoomlyRedContainer else DoomlySurfaceElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) DoomlyRed else DoomlyBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = mood.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = DoomlyWhite
                                        )
                                        Text(
                                            text = mood.rangeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DoomlyMutedGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                BrainStatusDisclaimerCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
