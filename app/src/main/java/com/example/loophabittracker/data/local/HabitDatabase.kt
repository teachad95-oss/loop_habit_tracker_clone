package com.example.loophabittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.loophabittracker.data.local.dao.HabitDao
import com.example.loophabittracker.data.local.entity.HabitEntity
import com.example.loophabittracker.data.local.entity.HabitRecordEntity

@Database(
    entities = [HabitEntity::class, HabitRecordEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract val habitDao: HabitDao
    
    companion object {
        const val DATABASE_NAME = "habit_tracker_db"
    }
}
