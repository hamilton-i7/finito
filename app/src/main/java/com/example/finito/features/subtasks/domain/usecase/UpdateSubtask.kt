package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import kotlin.jvm.Throws

class UpdateSubtask(
    private val repository: SubtaskRepository
) {
    @Throws(
        ResourceException.EmptyException::class,
        ResourceException.NegativeIdException::class,
        ResourceException.InvalidStateException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(subtask: Subtask): Int {
        if (subtask.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (!isValidId(subtask.subtaskId)) {
            throw ResourceException.NegativeIdException
        }
        repository.findOne(subtask.subtaskId) ?: ResourceException.NotFoundException
        return repository.updateMany(subtask)
    }
}