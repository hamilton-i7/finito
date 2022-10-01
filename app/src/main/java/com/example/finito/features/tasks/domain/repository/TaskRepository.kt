package com.example.finito.features.tasks.domain.repository

import com.example.finito.core.domain.Priority
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    suspend fun create(task: Task): Long

    suspend fun findAll(): List<Task>

    fun findTodayTasks(): Flow<List<TaskWithSubtasks>>

    fun findTomorrowTasks(): Flow<List<TaskWithSubtasks>>

    fun findUrgentTasks(priority: Priority = Priority.URGENT): Flow<List<TaskWithSubtasks>>

    suspend fun findTasksByBoardAmount(boardId: Int): Int

    suspend fun findTasksByBoard(boardId: Int): List<Task>

    suspend fun findOne(id: Int): TaskWithSubtasks?

    suspend fun update(task: Task)

    suspend fun updateMany(vararg tasks: Task)

    suspend fun remove(vararg tasks: Task)
}