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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class HabitUiModel(
    val habit: Habit,
    val recentRecords: List<HabitRecord?>, // Mon-Sun records
    val activeDays: List<Boolean> // Parallel array determining if interactable
)

data class DailySummary(
    val missedCount: Int,
    val totalPenalty: Float
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _currentWeekStart = MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart

    fun previousWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
    }

    fun nextWeek() {
        _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1)
    }

    fun resetToCurrentWeek() {
        _currentWeekStart.value = LocalDate.now().with(DayOfWeek.MONDAY)
    }

    private fun isHabitActive(habit: Habit, date: LocalDate): Boolean {
        return when (habit.frequencyDenominator) {
            "WEEK" -> {
                if (habit.selectedDays.isBlank()) return true
                habit.selectedDays.split(",").mapNotNull { it.toIntOrNull() }.contains(date.dayOfWeek.value)
            }
            "MONTH" -> {
                if (habit.selectedDays.isBlank()) return true
                habit.selectedDays.split(",").mapNotNull { it.toIntOrNull() }.contains(date.dayOfMonth)
            }
            else -> true
        }
    }

    val habitsWithRecords = combine(
        repository.getAllHabits(),
        _currentWeekStart
    ) { habits, weekStart ->
        habits.map { habit ->
            val records = repository.getRecordsForHabitSync(habit.id)
            
            val recentRecords = mutableListOf<HabitRecord?>()
            val activeDays = mutableListOf<Boolean>()
            
            for (i in 0..6) {
                val date = weekStart.plusDays(i.toLong())
                activeDays.add(isHabitActive(habit, date))
                recentRecords.add(records.find { it.date == date.toEpochDay() })
            }
            
            HabitUiModel(habit, recentRecords, activeDays)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummaries = combine(
        habitsWithRecords,
        _currentWeekStart
    ) { habitsData, _ ->
        val summaries = mutableListOf<DailySummary>()
        for (i in 0..6) {
            var missed = 0
            var penaltySum = 0f
            
            habitsData.forEach { model ->
                if (model.activeDays[i]) {
                    val recordForDay = model.recentRecords[i]
                    if (recordForDay != null && !recordForDay.isCompleted) {
                        missed++
                        penaltySum += model.habit.penalty
                    }
                }
            }
            summaries.add(DailySummary(missedCount = missed, totalPenalty = penaltySum))
        }
        summaries
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { DailySummary(0, 0f) })

    fun updateHabitValue(habitId: Int, dayOffsetIndex: Int, value: Float?) {
        viewModelScope.launch {
            val dateEpoch = _currentWeekStart.value.plusDays(dayOffsetIndex.toLong()).toEpochDay()
            val existingRecord = repository.getRecordForHabitOnDate(habitId, dateEpoch)
            
            val habit = repository.getAllHabits().first().find { it.id == habitId } ?: return@launch
            
            val isCompleted = if (habit.isMeasurable) {
                value != null && value >= habit.target
            } else {
                value != null && value > 0f 
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
