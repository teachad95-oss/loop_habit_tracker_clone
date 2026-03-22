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

    private val _isMeasurable = MutableStateFlow(true)
    val isMeasurable: StateFlow<Boolean> = _isMeasurable

    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question

    private val _unit = MutableStateFlow("")
    val unit: StateFlow<String> = _unit

    private val _target = MutableStateFlow("15") // Assuming "e.g. 15"
    val target: StateFlow<String> = _target

    private val _frequency = MutableStateFlow("Every day")
    val frequency: StateFlow<String> = _frequency

    private val _targetType = MutableStateFlow("At least")
    val targetType: StateFlow<String> = _targetType

    private val _reminder = MutableStateFlow("Off")
    val reminder: StateFlow<String> = _reminder

    private val _penalty = MutableStateFlow("")
    val penalty: StateFlow<String> = _penalty

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes

    fun updateBoolean(field: String, value: Boolean) {
        if (field == "isMeasurable") _isMeasurable.value = value
    }

    fun updateField(field: String, value: String) {
        when (field) {
            "name" -> _habitName.value = value
            "question" -> _question.value = value
            "unit" -> _unit.value = value
            "target" -> _target.value = value
            "frequency" -> _frequency.value = value
            "targetType" -> _targetType.value = value
            "reminder" -> _reminder.value = value
            "penalty" -> _penalty.value = value
            "notes" -> _notes.value = value
        }
    }

    fun onColorChanged(color: Int) {
        _habitColor.value = color
    }

    fun saveHabit(onComplete: () -> Unit) {
        viewModelScope.launch {
            if (_habitName.value.isNotBlank()) {
                val parsedTarget = _target.value.toFloatOrNull() ?: 0f
                val parsedPenalty = _penalty.value.toFloatOrNull() ?: 0f
                val freqDenom = if (_frequency.value == "Every day") "DAY" else "WEEK"

                val newHabit = Habit(
                    name = _habitName.value,
                    color = _habitColor.value,
                    frequencyNumerator = 7, // Default logic simplification
                    frequencyDenominator = freqDenom,
                    daysInterval = 0,
                    isMeasurable = _isMeasurable.value,
                    question = _question.value,
                    unit = _unit.value,
                    target = parsedTarget,
                    targetType = _targetType.value,
                    reminder = _reminder.value,
                    penalty = parsedPenalty,
                    notes = _notes.value,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertHabit(newHabit)
                onComplete()
            }
        }
    }
}
