package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class DeleteSubtask(
    private val repository: SubtaskRepository
) {

    suspend operator fun invoke(subtask: Subtask): Result<Unit, String> {
        if (!isValidId(subtask.subtaskId)) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        repository.findOne(subtask.subtaskId) ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
        return Result.Success(
            data = repository.removeMany(subtask)
        )
    }
}