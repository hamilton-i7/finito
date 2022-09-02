package com.example.finito.features.tasks.data.repository

import com.example.finito.core.Priority
import com.example.finito.features.tasks.data.dao.TaskDao
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskUpdate
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class TaskRepositoryImpl(
    private val dao: TaskDao
) : TaskRepository {
    override suspend fun create(task: Task) {
        return dao.create(task)
    }

    override fun findTodayTasks(): Flow<List<TaskWithSubtasks>> {
        return dao.findTodayTasks()
    }

    override fun findTomorrowTasks(): Flow<List<TaskWithSubtasks>> {
        return dao.findTomorrowTasks()
    }

    override fun findUrgentTasks(priority: Priority): Flow<List<TaskWithSubtasks>> {
        return dao.findUrgentTasks(priority)
    }

    override suspend fun findTasksByBoardAmount(boardId: Int): Int {
        return dao.findTasksByBoardAmount(boardId)
    }

    override suspend fun findTasksByBoard(boardId: Int): List<Task> {
        return dao.findTasksByBoard(boardId)
    }

    override suspend fun findOne(id: Int): Task? {
        return dao.findOne(id)
    }

    override suspend fun update(taskUpdate: TaskUpdate) {
        return dao.update(taskUpdate)
    }

    override suspend fun updateMany(vararg tasks: Task) {
        return dao.updateMany(*tasks)
    }

    override suspend fun remove(task: Task): Int {
        return dao.remove(task)
    }
}