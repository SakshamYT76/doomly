package com.example.data

import androidx.room.Entity

/**
 * Stores daily estimated scroll counts for each short-form platform.
 * Keyed by local calendar date ("YYYY-MM-DD") and platform ID ("INSTAGRAM", "YOUTUBE", "TIKTOK").
 * Only counts and timestamps are saved — never screen content.
 */
@Entity(
    tableName = "daily_scrolls",
    primaryKeys = ["date", "platform"]
)
data class DailyScrollEntity(
    val date: String,
    val platform: String,
    val estimatedScrolls: Int,
    val lastUpdatedEpochMs: Long
)

/**
 * Stores intraday hourly estimated scroll counts (hours 0..23) for bar graphs.
 */
@Entity(
    tableName = "hourly_scrolls",
    primaryKeys = ["date", "hour", "platform"]
)
data class HourlyScrollEntity(
    val date: String,
    val hour: Int,
    val platform: String,
    val estimatedScrolls: Int
)

/**
 * Tracks whether a daily warning milestone (200, 300, or 500 scrolls) has already
 * triggered a one-time-per-day system notification for the given date ("YYYY-MM-DD").
 */
@Entity(
    tableName = "daily_milestones",
    primaryKeys = ["date", "threshold"]
)
data class DailyMilestoneEntity(
    val date: String,
    val threshold: Int,
    val notifiedAtEpochMs: Long
)
