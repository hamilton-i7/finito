package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class FindOneSubtask(
    private val repository: SubtaskRepository
) {

    suspend operator fun invoke(id: Int): Result<Subtask, String> {
        if (!isValidId(id)) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        return repository.findOne(id)?.let { subtask ->
            Result.Success(data = subtask)
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
    }
}