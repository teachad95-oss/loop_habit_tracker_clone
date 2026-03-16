package com.example.loophabittracker.di

import com.example.loophabittracker.data.local.dao.HabitDao
import com.example.loophabittracker.data.repository.HabitRepositoryImpl
import com.example.loophabittracker.domain.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideHabitRepository(dao: HabitDao): HabitRepository {
        return HabitRepositoryImpl(dao)
    }
}
