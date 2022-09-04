package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
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
    suspend operator fun invoke(subtask: Subtask): Int {
        if (!isValidId(subtask.subtaskId)) {
            throw ResourceException.NegativeIdException
        }
        return repository.removeMany(subtask).let {
            if (it == 0) throw ResourceException.NotFoundException
            else it
        }
    }
}