package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class FindOneSubtask(
    private val repository: SubtaskRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(id: Int): Subtask {
        if (!isValidId(id)) {
            throw ResourceException.NegativeIdException
        }
        return repository.findOne(id) ?: throw ResourceException.NotFoundException
    }
}