package com.example.finito.features.subtasks.di

import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.subtasks.data.repository.SubtaskRepositoryImpl
import com.example.finito.features.subtasks.domain.entity.SubtaskRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SubtaskModule {

    fun provideSubtaskRepository(db: FinitoDatabase): SubtaskRepository {
        return SubtaskRepositoryImpl(db.subtaskDao)
    }
}