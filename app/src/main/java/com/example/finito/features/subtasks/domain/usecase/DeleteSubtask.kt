package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import kotlin.jvm.Throws

class DeleteSubtask(
    private val repository: SubtaskRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(subtask: Subtask) {
        if (!isValidId(subtask.subtaskId)) {
            throw ResourceException.NegativeIdException
        }
        repository.findOne(subtask.subtaskId) ?: throw ResourceException.NotFoundException
        return repository.removeMany(subtask)
    }
}