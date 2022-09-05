package com.example.finito.features.tasks.di

import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.data.repository.TaskRepositoryImpl
import com.example.finito.features.tasks.domain.repository.TaskRepository
import com.example.finito.features.tasks.domain.usecase.*
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
    fun provideTaskUseCases(
        taskRepository: TaskRepository,
        subtaskRepository: SubtaskRepository,
        boardRepository: BoardRepository,
    ): TaskUseCases {
        return TaskUseCases(
            createTask = CreateTask(taskRepository, subtaskRepository),
            findTodayTasks = FindTodayTasks(taskRepository, boardRepository),
            findTomorrowTasks = FindTomorrowTasks(taskRepository, boardRepository),
            findUrgentTasks = FindUrgentTasks(taskRepository, boardRepository),
            findOneTask = FindOneTask(taskRepository),
            updateTask = UpdateTask(taskRepository, subtaskRepository),
            deleteTask = DeleteTask(taskRepository),
            arrangeBoardTasks = ArrangeBoardTasks(taskRepository, subtaskRepository),
            arrangeTodayTasks = ArrangeTodayTasks(taskRepository, subtaskRepository),
            arrangeTomorrowTasks = ArrangeTomorrowTasks(taskRepository, subtaskRepository),
            arrangeUrgentTasks = ArrangeUrgentTasks(taskRepository, subtaskRepository)
        )
    }
}