package com.example.finito.features.tasks.domain.repository

import com.example.finito.core.Priority
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    suspend fun create(task: Task)

    fun findTodayTasks(): Flow<List<TaskWithSubtasks>>

    fun findTomorrowTasks(): Flow<List<TaskWithSubtasks>>

    fun findUrgentTasks(priority: Priority = Priority.URGENT): Flow<List<TaskWithSubtasks>>

    suspend fun update(task: Task)

    suspend fun remove(task: Task)
}