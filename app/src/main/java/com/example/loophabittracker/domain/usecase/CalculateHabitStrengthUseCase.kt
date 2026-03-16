package com.example.loophabittracker.domain.usecase

import com.example.loophabittracker.domain.model.HabitRecord
import java.time.LocalDate

class CalculateHabitStrengthUseCase {
    operator fun invoke(records: List<HabitRecord>, historyLengthDays: Int = 60): Float {
        var rawStrength = 0.0
        var maxPossibleStrength = 0.0
        val today = LocalDate.now().toEpochDay()
        
        val recordMap = records.associateBy { it.date }
        val decayFactor = 0.9
        
        for (i in 0 until historyLengthDays) {
            val dateEpoch = today - i
            val record = recordMap[dateEpoch]
            
            val term = Math.pow(decayFactor, i.toDouble())
            if (record != null && record.isCompleted) {
                rawStrength += term
            }
            maxPossibleStrength += term
        }
        
        if (maxPossibleStrength == 0.0) return 0f
        return ((rawStrength / maxPossibleStrength) * 100).toFloat()
    }
}
