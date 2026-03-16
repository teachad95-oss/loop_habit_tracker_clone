package com.example.loophabittracker.domain.model

data class Habit(
    val id: Int = 0,
    val name: String,
    val color: Int,
    val frequencyNumerator: Int,
    val frequencyDenominator: String,
    val daysInterval: Int,
    val createdAt: Long
)
