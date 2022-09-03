package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.repository.TaskRepository

class DeleteTask(
    private val repository: TaskRepository
) {
    @Throws(ResourceException.NegativeIdException::class)
    suspend operator fun invoke(task: Task): Int {
        if (!isValidId(task.taskId)) {
            throw ResourceException.NegativeIdException
        }
        arrangeTasks(task, repository)
        return repository.remove(task).let {
            if (it == 0) throw ResourceException.NotFoundException
            else it
        }
    }

    private suspend fun arrangeTasks(
        task: Task,
        repository: TaskRepository,
    ) {
        with(repository.findTasksByBoard(task.boardId).toMutableList()) {
            removeIf { it.taskId == task.taskId }
            mapIndexed { index, task ->
                task.copy(position = index)
            }.toTypedArray().also {
                repository.updateMany(*it)
            }
        }
    }
}