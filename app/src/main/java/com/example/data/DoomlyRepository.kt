package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Repository managing all local scroll estimates, hourly breakdowns, and milestone warnings.
 * Everything stays strictly on-device in SQLite.
 */
class DoomlyRepository(
    private val dao: DoomlyDao,
    private val preferences: DoomlyPreferences,
    private val onMilestoneCrossed: ((threshold: Int, totalScrolls: Int) -> Unit)? = null
) {

    fun observeDailyScrollsForDate(date: String): Flow<List<DailyScrollEntity>> =
        dao.observeDailyScrollsForDate(date)

    fun observeHourlyScrollsForDate(date: String): Flow<List<HourlyScrollEntity>> =
        dao.observeHourlyScrollsForDate(date)

    fun observeMilestonesForDate(date: String): Flow<List<DailyMilestoneEntity>> =
        dao.observeMilestonesForDate(date)

    fun observeScrollsInRange(startDate: String, endDate: String): Flow<List<DailyScrollEntity>> =
        dao.observeScrollsInRange(startDate, endDate)

    fun observeAllDailyScrolls(): Flow<List<DailyScrollEntity>> =
        dao.observeAllDailyScrolls()

    /**
     * Records an estimated short-form scroll event for the given platform at [timestampMs].
     * Automatically resolves the local calendar date and hour of day (handling midnight rollover)
     * and checks whether the 200, 300, or 500 daily milestone was crossed for the first time today.
     *
     * Returns the list of newly crossed milestone thresholds (if any).
     */
    suspend fun recordEstimatedScroll(
        platform: PlatformType,
        delta: Int = 1,
        timestampMs: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Int> {
        if (delta <= 0) return emptyList()
        if (!preferences.isPlatformTrackingEnabled(platform)) return emptyList()

        val zonedDateTime = Instant.ofEpochMilli(timestampMs).atZone(zoneId)
        val dateStr = zonedDateTime.toLocalDate().format(DATE_FORMATTER)
        val hour = zonedDateTime.hour.coerceIn(0, 23)

        val currentDaily = dao.getPlatformScrollForDate(dateStr, platform.id)
        val updatedDailyCount = (currentDaily?.estimatedScrolls ?: 0) + delta
        dao.upsertDailyScroll(
            DailyScrollEntity(
                date = dateStr,
                platform = platform.id,
                estimatedScrolls = updatedDailyCount,
                lastUpdatedEpochMs = timestampMs
            )
        )

        val currentHourly = dao.getHourlyScroll(dateStr, hour, platform.id)
        val updatedHourlyCount = (currentHourly?.estimatedScrolls ?: 0) + delta
        dao.upsertHourlyScroll(
            HourlyScrollEntity(
                date = dateStr,
                hour = hour,
                platform = platform.id,
                estimatedScrolls = updatedHourlyCount
            )
        )

        // Compute combined daily scrolls for dateStr to check 200 / 300 / 500 milestones
        val allToday = dao.getDailyScrollsForDate(dateStr)
        val combinedToday = allToday.sumOf { it.estimatedScrolls }

        val newlyCrossed = mutableListOf<Int>()
        for (threshold in BrainStatus.MILESTONE_THRESHOLDS) {
            if (combinedToday >= threshold) {
                val existing = dao.getMilestone(dateStr, threshold)
                if (existing == null) {
                    val rowId = dao.insertMilestone(
                        DailyMilestoneEntity(
                            date = dateStr,
                            threshold = threshold,
                            notifiedAtEpochMs = timestampMs
                        )
                    )
                    if (rowId != -1L) {
                        newlyCrossed.add(threshold)
                        if (preferences.state.value.milestoneNotificationsEnabled) {
                            onMilestoneCrossed?.invoke(threshold, combinedToday)
                        }
                    }
                }
            }
        }
        return newlyCrossed
    }

    /**
     * Resets all scroll counts, hourly bars, and milestone flags for a specific date (e.g., today).
     */
    suspend fun resetDateData(date: String) {
        dao.deleteDailyScrollsForDate(date)
        dao.deleteHourlyScrollsForDate(date)
        dao.deleteMilestonesForDate(date)
    }

    /**
     * Deletes all historical scroll counts, hourly breakdowns, and milestone logs across all dates.
     */
    suspend fun deleteAllData() {
        dao.deleteAllDailyScrolls()
        dao.deleteAllHourlyScrolls()
        dao.deleteAllMilestones()
    }

    companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        fun todayDateString(zoneId: ZoneId = ZoneId.systemDefault()): String =
            LocalDate.now(zoneId).format(DATE_FORMATTER)
    }
}
