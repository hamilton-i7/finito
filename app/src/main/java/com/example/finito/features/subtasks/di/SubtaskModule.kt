package com.example.finito.features.subtasks.di

import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.subtasks.data.repository.SubtaskRepositoryImpl
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.subtasks.domain.usecase.DeleteSubtask
import com.example.finito.features.subtasks.domain.usecase.FindOneSubtask
import com.example.finito.features.subtasks.domain.usecase.SubtaskUseCases
import com.example.finito.features.subtasks.domain.usecase.UpdateSubtask
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubtaskModule {

    @Provides
    @Singleton
    fun provideSubtaskRepository(db: FinitoDatabase): SubtaskRepository {
        return SubtaskRepositoryImpl(db.subtaskDao)
    }

    @Provides
    @Singleton
    fun provideSubtaskUseCases(repository: SubtaskRepository): SubtaskUseCases {
        return SubtaskUseCases(
            findOneSubtask = FindOneSubtask(repository),
            updateSubtask = UpdateSubtask(repository),
            deleteSubtask = DeleteSubtask(repository),
        )
    }
}