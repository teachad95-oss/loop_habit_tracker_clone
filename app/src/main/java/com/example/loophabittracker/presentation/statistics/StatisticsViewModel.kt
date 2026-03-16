package com.example.loophabittracker.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loophabittracker.domain.model.Habit
import com.example.loophabittracker.domain.repository.HabitRepository
import com.example.loophabittracker.domain.usecase.CalculateHabitStrengthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val habit: Habit? = null,
    val strength: Float = 0f,
    val totalCompletions: Int = 0
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
                
                _uiState.value = StatisticsUiState(
                    habit = habit,
                    strength = strength,
                    totalCompletions = totalCompletions
                )
            }
        }
    }
}
