package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.repository.TaskRepository

class DeleteTask(
    private val taskRepository: TaskRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.InvalidStateException::class
    )
    suspend operator fun invoke(vararg tasks: Task) {
        if (tasks.any { !isValidId(it.taskId) }) {
            throw ResourceException.NegativeIdException
        }
        return taskRepository.remove(*tasks).also {
            val tasksToArrange = mutableListOf<Task>()
            tasks.groupBy { it.boardId }.keys.forEach {
                tasksToArrange.addAll(taskRepository.findTasksByBoard(it))
            }
            arrangeTasks(tasksToArrange)
        }
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