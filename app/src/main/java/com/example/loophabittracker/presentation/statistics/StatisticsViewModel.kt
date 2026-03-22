package com.example.loophabittracker.presentation.statistics

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
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarDayData(
    val date: LocalDate,
    val value: Float?, // null if no record
    val isCompleted: Boolean,
    val isMissed: Boolean,
    val penalty: Float,
    val isFuture: Boolean
)

data class UiState(
    val habit: Habit? = null,
    val totalDaysInMonth: Int = 0,
    val totalFailures: Int = 0,
    val totalPenalty: Float = 0f,
    val calendarData: List<CalendarDayData> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _habitId = MutableStateFlow(-1)
    
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    fun previousMonth() { _currentMonth.value = _currentMonth.value.minusMonths(1) }
    fun nextMonth() { _currentMonth.value = _currentMonth.value.plusMonths(1) }
    fun resetToCurrentMonth() { _currentMonth.value = YearMonth.now() }

    fun loadStatistics(habitId: Int) {
        _habitId.value = habitId
    }

    private fun isHabitActive(habit: Habit, date: LocalDate): Boolean {
        // Shared logic with DashboardViewModel to determine if target applies this day
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

    val uiState = combine(_habitId, _currentMonth) { habitId, monthVar ->
        if (habitId == -1) return@combine UiState()

        val habit = repository.getAllHabits().first().find { it.id == habitId } ?: return@combine UiState()
        val allRecords = repository.getRecordsForHabitSync(habitId)
        
        val monthStartEpoch = monthVar.atDay(1).toEpochDay()
        val monthEndEpoch = monthVar.atEndOfMonth().toEpochDay()
        
        val monthRecords = allRecords.filter { it.date in monthStartEpoch..monthEndEpoch }

        val calendarData = mutableListOf<CalendarDayData>()
        var activeDaysCount = 0
        var totalFailures = 0
        var totalPenalty = 0f

        for (i in 1..monthVar.lengthOfMonth()) {
            val date = monthVar.atDay(i)
            val isFuture = date.isAfter(LocalDate.now())
            val record = monthRecords.find { it.date == date.toEpochDay() }
            val isActive = isHabitActive(habit, date)
            
            var isCompleted = false
            var isMissed = false
            var appliedPenalty = 0f

            if (isActive && !isFuture) {
                activeDaysCount++
                if (record != null) {
                    if (record.isCompleted) {
                        isCompleted = true
                    } else {
                        isMissed = true
                        appliedPenalty = habit.penalty
                        totalFailures++
                        totalPenalty += appliedPenalty
                    }
                } else {
                    // Implicitly missed if there is no record logged for an active past day?
                    // Typically missing logs might count as Missed for stats, let's say Yes.
                    isMissed = true
                    appliedPenalty = habit.penalty
                    totalFailures++
                    totalPenalty += appliedPenalty
                }
            } else if (isActive && isFuture) {
               // active but future, just count as total days possible? 
               activeDaysCount++
            }
            
            calendarData.add(
                CalendarDayData(
                    date = date,
                    value = record?.value,
                    isCompleted = isCompleted,
                    isMissed = isMissed,
                    penalty = appliedPenalty,
                    isFuture = isFuture
                )
            )
        }

        UiState(
            habit = habit,
            totalDaysInMonth = activeDaysCount,
            totalFailures = totalFailures,
            totalPenalty = totalPenalty,
            calendarData = calendarData
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
}
