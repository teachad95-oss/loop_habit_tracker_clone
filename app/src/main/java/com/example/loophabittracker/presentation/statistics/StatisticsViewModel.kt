package com.example.loophabittracker.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loophabittracker.domain.model.Habit
import com.example.loophabittracker.domain.model.HabitRecord
import com.example.loophabittracker.domain.repository.HabitRepository
import com.example.loophabittracker.domain.usecase.CalculateHabitStrengthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StatisticsUiState(
    val habit: Habit? = null,
    val strength: Float = 0f,
    val scoreMonth: Float = 0f,
    val scoreYear: Float = 0f,
    val totalCompletions: Int = 0,
    val recentScores: List<Float> = emptyList(),
    val historyCounts: List<Float> = emptyList(),
    val calendarRecords: Map<Long, Boolean> = emptyMap()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val calculateHabitStrength: CalculateHabitStrengthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadStatistics(habitId: Int) {
        viewModelScope.launch {
            val habits = repository.getAllHabits().first()
            val habit = habits.find { it.id == habitId }
            if (habit != null) {
                val records = repository.getRecordsForHabitSync(habitId)
                val strength = calculateHabitStrength(records)
                val totalCompletions = records.count { it.isCompleted }
                
                // Mock historical scores based on trailing calculations for the line chart (last 30 days)
                val recentScores = mutableListOf<Float>()
                for (i in 30 downTo 0) {
                    val subRecords = records.filter { it.date <= LocalDate.now().minusDays(i.toLong()).toEpochDay() }
                    recentScores.add(calculateHabitStrength(subRecords, historyLengthDays = 60))
                }

                // History counts (bar chart frequency by month). Simplified: last 12 months occurrences.
                val historyCounts = mutableListOf<Float>()
                for (i in 11 downTo 0) {
                    val startOfMonth = LocalDate.now().minusMonths(i.toLong()).withDayOfMonth(1).toEpochDay()
                    val endOfMonth = LocalDate.now().minusMonths(i.toLong()).withDayOfMonth(LocalDate.now().minusMonths(i.toLong()).lengthOfMonth()).toEpochDay()
                    historyCounts.add(records.count { it.isCompleted && it.date in startOfMonth..endOfMonth }.toFloat())
                }

                val calendarRecords = records.associate { it.date to it.isCompleted }

                _uiState.value = StatisticsUiState(
                    habit = habit,
                    strength = strength,
                    scoreMonth = recentScores.lastOrNull() ?: 0f,
                    scoreYear = recentScores.lastOrNull() ?: 0f, // Simplified
                    totalCompletions = totalCompletions,
                    recentScores = recentScores,
                    historyCounts = historyCounts,
                    calendarRecords = calendarRecords
                )
            }
        }
    }
}
