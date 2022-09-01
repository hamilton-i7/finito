package com.example.finito.features.tasks.di

import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.tasks.data.repository.TaskRepositoryImpl
import com.example.finito.features.tasks.domain.repository.TaskRepository
import com.example.finito.features.tasks.domain.usecase.CreateTask
import com.example.finito.features.tasks.domain.usecase.FindTodayTasks
import com.example.finito.features.tasks.domain.usecase.FindUpcomingTasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskModule {

    @Provides
    @Singleton
    fun provideTaskRepository(db: FinitoDatabase): TaskRepository {
        return TaskRepositoryImpl(db.taskDao)
    }

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases {
        return TaskUseCases(
            createTask = CreateTask(repository),
            findTodayTasks = FindTodayTasks(repository),
            findUpcomingTasks = FindUpcomingTasks(repository)
        )
    }
}