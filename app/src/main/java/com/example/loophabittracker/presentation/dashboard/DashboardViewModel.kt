package com.example.loophabittracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loophabittracker.domain.model.Habit
import com.example.loophabittracker.domain.model.HabitRecord
import com.example.loophabittracker.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitUiModel(
    val habit: Habit,
    val recentRecords: List<HabitRecord?> // last 7 days records
)

data class DailySummary(
    val missedCount: Int,
    val totalPenalty: Float
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate

    val habitsWithRecords = combine(
        repository.getAllHabits(),
        _currentDate
    ) { habits, date ->
        habits.map { habit ->
            val records = repository.getRecordsForHabitSync(habit.id)
            val recentRecords = (0L..6L).map { daysAgo ->
                val targetDate = date.toEpochDay() - daysAgo
                records.find { it.date == targetDate }
            }.reversed()
            
            HabitUiModel(habit, recentRecords)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummaries = combine(
        habitsWithRecords,
        _currentDate
    ) { habitsData, date ->
        val summaries = mutableListOf<DailySummary>()
        // Calculate for daysAgo from 6 downTo 0 to align with UI columns
        for (i in 6 downTo 0) {
            var missed = 0
            var penaltySum = 0f
            
            habitsData.forEach { model ->
                val recordForDay = model.recentRecords[6 - i]
                if (recordForDay != null && !recordForDay.isCompleted) {
                    missed++
                    penaltySum += model.habit.penalty
                }
            }
            summaries.add(DailySummary(missedCount = missed, totalPenalty = penaltySum))
        }
        summaries
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { DailySummary(0, 0f) })

    fun updateHabitValue(habitId: Int, daysAgo: Int, value: Float?) {
        viewModelScope.launch {
            val dateEpoch = _currentDate.value.toEpochDay() - daysAgo
            val existingRecord = repository.getRecordForHabitOnDate(habitId, dateEpoch)
            
            val habit = repository.getAllHabits().first().find { it.id == habitId } ?: return@launch
            
            val isCompleted = if (habit.isMeasurable) {
                value != null && value >= habit.target
            } else {
                value != null && value > 0f // In yes/no, value > 0 means Yes
            }
            
            val recordToInsert = HabitRecord(
                id = existingRecord?.id ?: 0,
                habitId = habitId,
                date = dateEpoch,
                isCompleted = isCompleted,
                value = value ?: (existingRecord?.value ?: 0f),
                timestamp = System.currentTimeMillis()
            )
            repository.insertRecord(recordToInsert)
        }
    }
}
