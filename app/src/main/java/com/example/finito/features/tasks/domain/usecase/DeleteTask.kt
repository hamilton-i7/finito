package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.repository.TaskRepository

class DeleteTask(
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(vararg tasks: Task): Result<Unit, String> {
        if (tasks.any { !isValidId(it.taskId) }) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        val formattedTasks = tasks.map { it.copy(boardPosition = null) }
        return Result.Success(
            data = taskRepository.remove(*formattedTasks.toTypedArray()).also {
                val tasksToArrange = mutableListOf<Task>()
                formattedTasks.groupBy { it.boardId }.keys.forEach {
                    tasksToArrange.addAll(taskRepository.findTasksByBoard(it))
                }
                arrangeTasks(tasksToArrange)
            }
        )
    }

    private suspend fun arrangeTasks(tasks: List<Task>) {
        val positionsMap = mutableMapOf<Int, Int>()
        tasks.map {
            positionsMap[it.boardId] =
                if (positionsMap[it.boardId] == null) 0 else positionsMap[it.boardId]!! + 1
            it.copy(boardPosition = positionsMap[it.boardId]!!)
        }.let { taskRepository.updateMany(*it.toTypedArray()) }
    }
}