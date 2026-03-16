package com.example.loophabittracker.domain.usecase

import android.content.Context
import android.os.Environment
import com.example.loophabittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class ExportDatabaseToCsvUseCase(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(context: Context): Result<String> {
        return try {
            val habits = repository.getAllHabits().first()
            val fileName = "LoopHabitTrackerBackup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            FileWriter(file).use { writer ->
                writer.append("HabitId,HabitName,Date,IsCompleted\n")
                
                for (habit in habits) {
                    val records = repository.getRecordsForHabitSync(habit.id)
                    for (record in records) {
                        val dateStr = LocalDate.ofEpochDay(record.date).toString()
                        writer.append("${habit.id},${habit.name},${dateStr},${record.isCompleted}\n")
                    }
                }
            }
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
