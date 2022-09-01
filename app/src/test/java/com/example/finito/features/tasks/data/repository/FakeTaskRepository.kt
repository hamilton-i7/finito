package com.example.finito.features.tasks.data.repository

import com.example.finito.core.Priority
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskUpdate
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<TaskWithSubtasks>()

    override suspend fun create(task: Task) {
        TODO("Not yet implemented")
    }

    fun create(taskWithSubtasks: TaskWithSubtasks) {
        tasks.add(taskWithSubtasks)
    }

    override fun findTodayTasks(): Flow<List<TaskWithSubtasks>> {
        val today = LocalDate.now()

        return flow {
            emit(
                tasks.filter {
                    val date = it.task.date
                    date != null && date.isEqual(today)
                }
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
                }
            )
        }
    }

    override fun findUrgentTasks(priority: Priority): Flow<List<TaskWithSubtasks>> {
        return flow {
            emit(
                tasks.filter {
                    it.task.priority == Priority.URGENT
                }
            )
        }
    }

    override suspend fun findTasksByBoardAmount(boardId: Int): Int {
        TODO("Not yet implemented")
    }

    override suspend fun findTasksByBoard(boardId: Int): List<Task> {
        TODO("Not yet implemented")
    }

    override suspend fun findOne(id: Int): Task? {
        TODO("Not yet implemented")
    }

    override suspend fun update(taskUpdate: TaskUpdate) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMany(vararg tasks: Task) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(task: Task) {
        TODO("Not yet implemented")
    }
}