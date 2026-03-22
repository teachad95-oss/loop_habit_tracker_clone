package com.example.loophabittracker.domain.model

data class Habit(
    val id: Int = 0,
    val name: String,
    val color: Int,
    val frequencyNumerator: Int,
    val frequencyDenominator: String,
    val daysInterval: Int,
    val selectedDays: String = "",
    val isMeasurable: Boolean = false,
    val question: String = "",
    val unit: String = "",
    val target: Float = 0f,
    val targetType: String = "",
    val reminder: String = "",
    val penalty: Float = 0f,
    val notes: String = "",
    val createdAt: Long
)
