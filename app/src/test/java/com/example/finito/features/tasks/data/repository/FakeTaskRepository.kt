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
    private val tasks = mutableListOf<TaskWithSubtasks>()

    override suspend fun create(task: Task): Long {
        val taskId = tasks.map { it.task.taskId }.maxOrNull()?.let { it + 1 } ?: 1
        tasks.add(TaskWithSubtasks(
            task = task.copy(taskId = taskId)
        ))
        return taskId.toLong()
    }

    fun create(taskWithSubtasks: TaskWithSubtasks) {
        tasks.add(taskWithSubtasks)
    }

    fun findAll() = tasks.toList()

    override fun findTodayTasks(): Flow<List<TaskWithSubtasks>> {
        val today = LocalDate.now()

        return flow {
            emit(
                tasks.filter {
                    val date = it.task.date
                    date != null && date.isEqual(today)
                }.map { TaskWithSubtasks(task = it.task) }
            )
        }
    }

    override fun findTomorrowTasks(): Flow<List<TaskWithSubtasks>> {
        val tomorrow = LocalDate.now().plusDays(1)
        return flow {
            emit(
                tasks.filter {
                    val date = it.task.date
                    date != null && date.isEqual(tomorrow)
                }.map { TaskWithSubtasks(task = it.task) }
            )
        }
    }

    override fun findUrgentTasks(priority: Priority): Flow<List<TaskWithSubtasks>> {
        return flow {
            emit(
                tasks.filter {
                    it.task.priority == Priority.URGENT
                }.map { TaskWithSubtasks(task = it.task) }
            )
        }
    }

    override suspend fun findTasksByBoardAmount(boardId: Int): Int {
        return tasks.count { it.task.boardId == boardId }
    }

    override suspend fun findTasksByBoard(boardId: Int): List<Task> {
        return tasks.filter { it.task.boardId == boardId }
            .map { it.task }
            .sortedBy { it.position }
    }

    override suspend fun findOne(id: Int): TaskWithSubtasks? {
        return tasks.find { it.task.taskId == id }
    }

    override suspend fun update(taskUpdate: TaskUpdate) {
        tasks.find { it.task.taskId == taskUpdate.taskId }?.let { taskWithSubtasks ->
            tasks.set(
                index = tasks.indexOfFirst { it.task.taskId == taskUpdate.taskId },
                element = taskWithSubtasks.copy(
                    taskUpdate.toTask().copy(position = taskWithSubtasks.task.position)
                )
            )
        }
    }

    override suspend fun updateMany(vararg tasks: Task) {
        val idsMap = this.tasks.groupBy { it.task.taskId }
        for (task in tasks) {
            if (idsMap[task.taskId] == null) continue
            val taskToUpdate = this.tasks.first { it.task.taskId == task.taskId }
            this.tasks.set(
                index = this.tasks.indexOf(taskToUpdate),
                element = taskToUpdate.copy(task = task)
            )
        }
    }

    override suspend fun remove(task: Task): Int {
        tasks.indexOfFirst { it.task.taskId == task.taskId }.let {
            if (it == -1) return 0
            tasks.removeAt(it)
            return 1
        }
    }
}