package com.example.loophabittracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loophabittracker.data.local.entity.HabitEntity
import com.example.loophabittracker.data.local.entity.HabitRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HabitRecordEntity)

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY date DESC")
    fun getRecordsForHabit(habitId: Int): Flow<List<HabitRecordEntity>>

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId")
    suspend fun getRecordsForHabitSync(habitId: Int): List<HabitRecordEntity>

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getRecordForHabitOnDate(habitId: Int, date: Long): HabitRecordEntity?
}
