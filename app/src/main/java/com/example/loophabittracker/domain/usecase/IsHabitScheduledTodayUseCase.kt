package com.example.loophabittracker.domain.usecase

import com.example.loophabittracker.domain.model.Habit
import java.time.LocalDate

class IsHabitScheduledTodayUseCase {
    operator fun invoke(habit: Habit, date: LocalDate): Boolean {
        return when (habit.frequencyDenominator) {
            "WEEK" -> true // Flexible week schedule
            "MONTH" -> true // Flexible month schedule
            "DAYS_INTERVAL" -> {
                val createdDate = LocalDate.ofEpochDay(habit.createdAt / (1000 * 60 * 60 * 24))
                val daysBetween = date.toEpochDay() - createdDate.toEpochDay()
                daysBetween >= 0 && daysBetween % habit.daysInterval == 0L
            }
            else -> true
        }
    }
}
