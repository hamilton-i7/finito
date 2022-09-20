package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository

class FindOneTask(
    private val repository: TaskRepository
) {

    suspend operator fun invoke(id: Int): Result<TaskWithSubtasks, String> {
        if (!isValidId(id)) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        return repository.findOne(id)?.let { taskWithSubtasks ->
            Result.Success(
                data = taskWithSubtasks.copy(
                    subtasks = taskWithSubtasks.subtasks.sortedBy { it.position }
                )
            )
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
    }
}