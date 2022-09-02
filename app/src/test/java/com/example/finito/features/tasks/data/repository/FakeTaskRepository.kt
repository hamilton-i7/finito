package com.example.finito.features.tasks.data.repository

import com.example.finito.core.Priority
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskUpdate
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.toTask
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()

    override suspend fun create(task: Task) {
        tasks.add(task)
    }

    fun findAll() = tasks.toList()

    override fun findTodayTasks(): Flow<List<TaskWithSubtasks>> {
        val today = LocalDate.now()

        return flow {
            emit(
                tasks.filter {
                    val date = it.date
                    date != null && date.isEqual(today)
                }.map { TaskWithSubtasks(task = it) }
            )
        }
    }

    override fun findTomorrowTasks(): Flow<List<TaskWithSubtasks>> {
        val tomorrow = LocalDate.now().plusDays(1)
        return flow {
            emit(
                tasks.filter {
                    val date = it.date
                    date != null && date.isEqual(tomorrow)
                }.map { TaskWithSubtasks(task = it) }
            )
        }
    }

    override fun findUrgentTasks(priority: Priority): Flow<List<TaskWithSubtasks>> {
        return flow {
            emit(
                tasks.filter {
                    it.priority == Priority.URGENT
                }.map { TaskWithSubtasks(task = it) }
            )
        }
    }

    override suspend fun findTasksByBoardAmount(boardId: Int): Int {
        return tasks.count { it.boardId == boardId }
    }

    override suspend fun findTasksByBoard(boardId: Int): List<Task> {
        return tasks.filter { it.boardId == boardId }.sortedBy { it.position }
    }

    override suspend fun findOne(id: Int): Task? {
        return tasks.find { it.taskId == id }
    }

    override suspend fun update(taskUpdate: TaskUpdate) {
        tasks.find { it.taskId == taskUpdate.taskId }?.let { task ->
            tasks.set(
                index = tasks.indexOfFirst { it.taskId == taskUpdate.taskId },
                element = taskUpdate.toTask().copy(position = task.position)
            )
        }
    }

    override suspend fun updateMany(vararg tasks: Task) {
        val idsMap = this.tasks.groupBy { it.taskId }
        for (task in tasks) {
            if (idsMap[task.taskId] == null) continue
            this.tasks.set(
                index = this.tasks.indexOfFirst { it.taskId == task.taskId },
                element = task
            )
        }
    }

    override suspend fun remove(task: Task): Int {
        tasks.indexOfFirst { it.taskId == task.taskId }.let {
            if (it == -1) return 0
            tasks.removeAt(it)
            return 1
        }
    }
}