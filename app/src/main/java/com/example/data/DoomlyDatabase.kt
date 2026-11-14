package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyScrollEntity::class,
        HourlyScrollEntity::class,
        DailyMilestoneEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DoomlyDatabase : RoomDatabase() {

    abstract fun doomlyDao(): DoomlyDao

    companion object {
        @Volatile
        private var INSTANCE: DoomlyDatabase? = null

        fun getInstance(context: Context): DoomlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DoomlyDatabase::class.java,
                    "doomly_local.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun setTestInstance(database: DoomlyDatabase) {
            INSTANCE = database
        }
    }
}
