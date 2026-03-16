package com.example.loophabittracker.domain.repository

import com.example.loophabittracker.domain.model.Habit
import com.example.loophabittracker.domain.model.HabitRecord
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habit: Habit): Long
    suspend fun deleteHabit(habit: Habit)
    fun getAllHabits(): Flow<List<Habit>>
    
    suspend fun insertRecord(record: HabitRecord)
    fun getRecordsForHabit(habitId: Int): Flow<List<HabitRecord>>
    suspend fun getRecordsForHabitSync(habitId: Int): List<HabitRecord>
    suspend fun getRecordForHabitOnDate(habitId: Int, date: Long): HabitRecord?
}
