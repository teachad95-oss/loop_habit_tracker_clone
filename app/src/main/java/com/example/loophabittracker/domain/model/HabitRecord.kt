package com.example.loophabittracker.domain.model

data class HabitRecord(
    val id: Int = 0,
    val habitId: Int,
    val date: Long,
    val isCompleted: Boolean,
    val value: Float = 0f,
    val timestamp: Long
)
