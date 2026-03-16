package com.example.loophabittracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: Int, // ARGB
    val frequencyNumerator: Int,
    val frequencyDenominator: String, // "WEEK", "MONTH", "DAYS_INTERVAL"
    val daysInterval: Int, // Only used if denominator is "DAYS"
    val createdAt: Long // System.currentTimeMillis()
)
