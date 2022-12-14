package com.example.finito.features.tasks.data.repository

import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.features.subtasks.data.repository.FakeSubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class FakeTaskRepository(
    private val subtaskRepository: FakeSubtaskRepository
) : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var taskId = 1
    private val boardIds = mutableMapOf<Int, Int>()

    override suspend fun create(task: Task): Long {
        boardIds.apply {
            val boardId = task.boardId
            this[boardId] = if (this[boardId] == null) 0 else this[boardId]!! + 1
        }
        tasks.add(task.copy(
            taskId = taskId,
            boardPosition = boardIds[task.boardId]!!
        ))
        taskId++
        return taskId.toLong()
    }

    override suspend fun findAll(): List<Task> {
        return tasks.toList()
    }

    override fun findTodayTasks(): Flow<List<TaskWithSubtasks>> {
        val today = LocalDate.now()

        return flow {
            emit(
                tasks.filter {
                    val date = it.date
                    date != null && date.isEqual(today)
                }.map {
                    val subtasks = subtaskRepository.findAllByTaskId(it.taskId)
                    TaskWithSubtasks(task = it, subtasks = subtasks)
                }
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
                }.map {
                    val subtasks = subtaskRepository.findAllByTaskId(it.taskId)
                    TaskWithSubtasks(task = it, subtasks = subtasks)
                }
            )
        }
    }

    override fun findUrgentTasks(priority: Priority): Flow<List<TaskWithSubtasks>> {
        return flow {
            emit(
                tasks.filter {
                    it.priority == Priority.URGENT
                }.map {
                    val subtasks = subtaskRepository.findAllByTaskId(it.taskId)
                    TaskWithSubtasks(task = it, subtasks = subtasks)
                }
            )
        }
    }

    override suspend fun findTasksByBoardAmount(boardId: Int): Int {
        return tasks.count { it.boardId == boardId }
    }

    override suspend fun findTasksByBoard(boardId: Int): List<Task> {
        return tasks.filter { it.boardId == boardId }
            .map { it }
            .sortedBy { it.boardPosition }
    }

    override suspend fun findOne(id: Int): TaskWithSubtasks? {
        val task = tasks.find { it.taskId == id } ?: return null
        return task.let {
            val subtasks = subtaskRepository.findAllByTaskId(it.taskId)
            TaskWithSubtasks(
                task = it,
                subtasks = subtasks
            )
        }
    }

    override suspend fun update(task: Task) {
        tasks.find { it.taskId == task.taskId }?.let { foundTask ->
            tasks.set(
                index = tasks.indexOfFirst { it.taskId == task.taskId },
                element = task.copy(boardPosition = foundTask.boardPosition)
            )
        }
    }

    override suspend fun updateMany(vararg tasks: Task) {
        val idsMap = tasks.groupBy { it.taskId }
        for (task in tasks) {
            if (idsMap[task.taskId] == null) continue
            this.tasks.set(
                index = this.tasks.indexOfFirst { it.taskId == task.taskId },
                element = task
            )
        }
    }

    override suspend fun remove(vararg tasks: Task) {
        this.tasks.removeAll(tasks.toSet())
    }
}