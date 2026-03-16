package com.example.loophabittracker.di

import com.example.loophabittracker.domain.repository.HabitRepository
import com.example.loophabittracker.domain.usecase.CalculateHabitStrengthUseCase
import com.example.loophabittracker.domain.usecase.ExportDatabaseToCsvUseCase
import com.example.loophabittracker.domain.usecase.IsHabitScheduledTodayUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideCalculateHabitStrengthUseCase(): CalculateHabitStrengthUseCase {
        return CalculateHabitStrengthUseCase()
    }

    @Provides
    @Singleton
    fun provideExportDatabaseToCsvUseCase(repository: HabitRepository): ExportDatabaseToCsvUseCase {
        return ExportDatabaseToCsvUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideIsHabitScheduledTodayUseCase(): IsHabitScheduledTodayUseCase {
        return IsHabitScheduledTodayUseCase()
    }
}
