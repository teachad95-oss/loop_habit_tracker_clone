package com.example.loophabittracker.data.repository

import com.example.loophabittracker.data.local.dao.HabitDao
import com.example.loophabittracker.data.local.entity.HabitEntity
import com.example.loophabittracker.data.local.entity.HabitRecordEntity
import com.example.loophabittracker.domain.model.Habit
import com.example.loophabittracker.domain.model.HabitRecord
import com.example.loophabittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl(
    private val dao: HabitDao
) : HabitRepository {

    override suspend fun insertHabit(habit: Habit): Long {
        val entity = HabitEntity(
            id = habit.id,
            name = habit.name,
            color = habit.color,
            frequencyNumerator = habit.frequencyNumerator,
            frequencyDenominator = habit.frequencyDenominator,
            daysInterval = habit.daysInterval,
            createdAt = habit.createdAt
        )
        return dao.insertHabit(entity)
    }

    override suspend fun deleteHabit(habit: Habit) {
        val entity = HabitEntity(
            id = habit.id,
            name = habit.name,
            color = habit.color,
            frequencyNumerator = habit.frequencyNumerator,
            frequencyDenominator = habit.frequencyDenominator,
            daysInterval = habit.daysInterval,
            createdAt = habit.createdAt
        )
        dao.deleteHabit(entity)
    }

    override fun getAllHabits(): Flow<List<Habit>> {
        return dao.getAllHabits().map { entities ->
            entities.map { entity ->
                Habit(
                    id = entity.id,
                    name = entity.name,
                    color = entity.color,
                    frequencyNumerator = entity.frequencyNumerator,
                    frequencyDenominator = entity.frequencyDenominator,
                    daysInterval = entity.daysInterval,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun insertRecord(record: HabitRecord) {
        val entity = HabitRecordEntity(
            id = record.id,
            habitId = record.habitId,
            date = record.date,
            isCompleted = record.isCompleted,
            timestamp = record.timestamp
        )
        dao.insertRecord(entity)
    }

    override fun getRecordsForHabit(habitId: Int): Flow<List<HabitRecord>> {
        return dao.getRecordsForHabit(habitId).map { entities ->
            entities.map {
                HabitRecord(it.id, it.habitId, it.date, it.isCompleted, it.timestamp)
            }
        }
    }

    override suspend fun getRecordsForHabitSync(habitId: Int): List<HabitRecord> {
        return dao.getRecordsForHabitSync(habitId).map {
            HabitRecord(it.id, it.habitId, it.date, it.isCompleted, it.timestamp)
        }
    }

    override suspend fun getRecordForHabitOnDate(habitId: Int, date: Long): HabitRecord? {
        val entity = dao.getRecordForHabitOnDate(habitId, date) ?: return null
        return HabitRecord(entity.id, entity.habitId, entity.date, entity.isCompleted, entity.timestamp)
    }
}
