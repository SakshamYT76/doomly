package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DoomlyDao {

    @Query("SELECT * FROM daily_scrolls WHERE date = :date")
    fun observeDailyScrollsForDate(date: String): Flow<List<DailyScrollEntity>>

    @Query("SELECT * FROM daily_scrolls WHERE date = :date")
    suspend fun getDailyScrollsForDate(date: String): List<DailyScrollEntity>

    @Query("SELECT * FROM daily_scrolls WHERE date = :date AND platform = :platform LIMIT 1")
    suspend fun getPlatformScrollForDate(date: String, platform: String): DailyScrollEntity?

    @Query("SELECT * FROM daily_scrolls WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun observeScrollsInRange(startDate: String, endDate: String): Flow<List<DailyScrollEntity>>

    @Query("SELECT * FROM daily_scrolls ORDER BY date DESC")
    fun observeAllDailyScrolls(): Flow<List<DailyScrollEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyScroll(entity: DailyScrollEntity)

    @Query("SELECT * FROM hourly_scrolls WHERE date = :date ORDER BY hour ASC")
    fun observeHourlyScrollsForDate(date: String): Flow<List<HourlyScrollEntity>>

    @Query("SELECT * FROM hourly_scrolls WHERE date = :date AND hour = :hour AND platform = :platform LIMIT 1")
    suspend fun getHourlyScroll(date: String, hour: Int, platform: String): HourlyScrollEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHourlyScroll(entity: HourlyScrollEntity)

    @Query("SELECT * FROM daily_milestones WHERE date = :date ORDER BY threshold ASC")
    fun observeMilestonesForDate(date: String): Flow<List<DailyMilestoneEntity>>

    @Query("SELECT * FROM daily_milestones WHERE date = :date AND threshold = :threshold LIMIT 1")
    suspend fun getMilestone(date: String, threshold: Int): DailyMilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMilestone(entity: DailyMilestoneEntity): Long

    @Query("DELETE FROM daily_scrolls WHERE date = :date")
    suspend fun deleteDailyScrollsForDate(date: String)

    @Query("DELETE FROM hourly_scrolls WHERE date = :date")
    suspend fun deleteHourlyScrollsForDate(date: String)

    @Query("DELETE FROM daily_milestones WHERE date = :date")
    suspend fun deleteMilestonesForDate(date: String)

    @Query("DELETE FROM daily_scrolls")
    suspend fun deleteAllDailyScrolls()

    @Query("DELETE FROM hourly_scrolls")
    suspend fun deleteAllHourlyScrolls()

    @Query("DELETE FROM daily_milestones")
    suspend fun deleteAllMilestones()
}
