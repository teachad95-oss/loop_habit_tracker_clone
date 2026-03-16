package com.example.loophabittracker.presentation.add_edit_habit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loophabittracker.domain.model.Habit
import com.example.loophabittracker.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _habitName = MutableStateFlow("")
    val habitName: StateFlow<String> = _habitName

    private val _habitColor = MutableStateFlow(Color.Blue.toArgb())
    val habitColor: StateFlow<Int> = _habitColor

    fun onNameChanged(name: String) {
        _habitName.value = name
    }

    fun onColorChanged(color: Int) {
        _habitColor.value = color
    }

    fun saveHabit(onComplete: () -> Unit) {
        viewModelScope.launch {
            if (_habitName.value.isNotBlank()) {
                val newHabit = Habit(
                    name = _habitName.value,
                    color = _habitColor.value,
                    frequencyNumerator = 7,
                    frequencyDenominator = "WEEK",
                    daysInterval = 0,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertHabit(newHabit)
                onComplete()
            }
        }
    }
}
