package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository

class FindOneTask(
    private val repository: TaskRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(id: Int): TaskWithSubtasks {
        if (!isValidId(id)) {
            throw ResourceException.NegativeIdException
        }
        return repository.findOne(id)?.let { taskWithSubtasks ->
            taskWithSubtasks.copy(
                subtasks = taskWithSubtasks.subtasks.sortedBy { it.position }
            )
        } ?: throw ResourceException.NotFoundException
    }
}