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

    fun toggleHabit(habitId: Int, daysAgo: Int) {
        viewModelScope.launch {
            val dateEpoch = _currentDate.value.toEpochDay() - daysAgo
            val existingRecord = repository.getRecordForHabitOnDate(habitId, dateEpoch)
            
            val newIsCompleted = if (existingRecord != null) !existingRecord.isCompleted else true
            
            val recordToInsert = HabitRecord(
                id = existingRecord?.id ?: 0,
                habitId = habitId,
                date = dateEpoch,
                isCompleted = newIsCompleted,
                value = existingRecord?.value ?: 0f,
                timestamp = System.currentTimeMillis()
            )
            repository.insertRecord(recordToInsert)
        }
    }

    fun updateHabitValue(habitId: Int, daysAgo: Int, value: Float) {
        viewModelScope.launch {
            val dateEpoch = _currentDate.value.toEpochDay() - daysAgo
            val existingRecord = repository.getRecordForHabitOnDate(habitId, dateEpoch)
            
            val habit = repository.getAllHabits().first().find { it.id == habitId }
            val isCompleted = habit != null && value >= habit.target
            
            val recordToInsert = HabitRecord(
                id = existingRecord?.id ?: 0,
                habitId = habitId,
                date = dateEpoch,
                isCompleted = isCompleted,
                value = value,
                timestamp = System.currentTimeMillis()
            )
            repository.insertRecord(recordToInsert)
        }
    }
}
