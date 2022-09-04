package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.repository.TaskRepository

class DeleteTask(
    private val repository: TaskRepository
) {
    @Throws(ResourceException.NegativeIdException::class)
    suspend operator fun invoke(task: Task) {
        if (!isValidId(task.taskId)) {
            throw ResourceException.NegativeIdException
        }
        if (repository.findOne(task.taskId) == null) throw ResourceException.NotFoundException

        arrangeTasks(task, repository)
        return repository.remove(task)
    }

    private suspend fun arrangeTasks(
        task: Task,
        repository: TaskRepository,
    ) {
        with(repository.findTasksByBoard(task.boardId).toMutableList()) {
            removeAt(indexOfFirst { it.taskId == task.taskId })
            mapIndexed { index, task ->
                task.copy(position = index)
            }.toTypedArray().also {
                repository.updateMany(*it)
            }
        }
    }
}