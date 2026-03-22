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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class UiState(
    val habit: Habit? = null,
    val strength: Float = 0f,
    val scoreMonth: Float = 0f,
    val scoreYear: Float = 0f,
    val totalCompletions: Int = 0,
    val totalPenalty: Float = 0f,
    val weekCompletions: Int = 0,
    val weekPenalty: Float = 0f,
    val recentScores: List<Float> = emptyList(),
    val historyCounts: List<Float> = emptyList(),
    val calendarRecords: Map<Long, Boolean> = emptyMap()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _habitId = MutableStateFlow(-1)
    
    private val _currentWeekStart = MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart

    fun previousWeek() { _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1) }
    fun nextWeek() { _currentWeekStart.value = _currentWeekStart.value.plusWeeks(1) }

    fun loadStatistics(habitId: Int) {
        _habitId.value = habitId
    }

    val uiState = combine(_habitId, _currentWeekStart) { habitId, weekStart ->
        if (habitId == -1) return@combine UiState()

        val habit = repository.getAllHabits().first().find { it.id == habitId } ?: return@combine UiState()
        val allRecords = repository.getRecordsForHabitSync(habitId)
        
        val weekEnd = weekStart.plusDays(6)
        val weekEndEpoch = weekEnd.toEpochDay()
        val weekStartEpoch = weekStart.toEpochDay()

        // Lifetimes
        val strength = calculateHabitStrength(allRecords)
        val totalCompletions = allRecords.count { it.isCompleted }
        val totalPenalty = allRecords.filter { !it.isCompleted }.sumOf { habit.penalty.toDouble() }.toFloat()

        // Selected Week Specifics
        val weekRecords = allRecords.filter { it.date in weekStartEpoch..weekEndEpoch }
        val weekCompletions = weekRecords.count { it.isCompleted }
        val weekPenalty = weekRecords.filter { !it.isCompleted }.sumOf { habit.penalty.toDouble() }.toFloat()

        // Mock historical scores trailing from End-Of-Week
        val recentScores = mutableListOf<Float>()
        for (i in 30 downTo 0) {
            val target = weekEnd.minusDays(i.toLong()).toEpochDay()
            val filtered = allRecords.filter { it.date <= target }
            recentScores.add(calculateHabitStrength(filtered))
        }

        // Mock history bar chart trailing from the month of the End-Of-Week
        val historyCounts = mutableListOf<Float>()
        for (i in 5 downTo 0) { // last 6 months
            val targetMonth = weekEnd.minusMonths(i.toLong())
            val yM = YearMonth.from(targetMonth)
            val monthEndEpoch = yM.atEndOfMonth().toEpochDay()
            val monthStartEpoch = yM.atDay(1).toEpochDay()
            val c = allRecords.count { it.date in monthStartEpoch..monthEndEpoch && it.isCompleted }
            historyCounts.add(c.toFloat())
        }

        // Calendar Map relative for heatmap plotting if needed, but we can pass all
        val calendarRecords = allRecords.associate { it.date to it.isCompleted }

        UiState(
            habit = habit,
            strength = strength,
            scoreMonth = recentScores.lastOrNull() ?: 0f,
            scoreYear = recentScores.lastOrNull() ?: 0f,
            totalCompletions = totalCompletions,
            totalPenalty = totalPenalty,
            weekCompletions = weekCompletions,
            weekPenalty = weekPenalty,
            recentScores = recentScores,
            historyCounts = historyCounts,
            calendarRecords = calendarRecords
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    private fun calculateHabitStrength(records: List<HabitRecord>): Float {
        // Simplified scoring
        var score = 0f
        val sorted = records.sortedBy { it.date }
        for (rec in sorted) {
            if (rec.isCompleted) score += 5f else score -= 2f
            if (score > 100f) score = 100f
            if (score < 0f) score = 0f
        }
        return score
    }
}
