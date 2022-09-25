package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class UpdateSubtask(
    private val repository: SubtaskRepository
) {
    suspend operator fun invoke(subtask: Subtask): Result<Unit, String> {
        if (subtask.name.isBlank()) {
            return Result.Error(ErrorMessages.EMPTY_NAME)
        }
        if (!isValidId(subtask.subtaskId)) {
            return Result.Error(ErrorMessages.INVALID_ID)
        }
        val positionedSubtask = repository.findOne(subtask.subtaskId)?.let {
            if (!changedCompletedState(it, subtask)) return@let subtask
            if (subtask.completed) return@let subtask.copy(position = null)
            return@let subtask.copy(
                position = repository.findAllByTaskId(subtask.taskId).filter { subtask1 ->
                    !subtask1.completed
                }.size
            )
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
        return Result.Success(
            data = repository.updateMany(positionedSubtask)
        )
    }

    private fun changedCompletedState(oldSubTask: Subtask, newSubtask: Subtask): Boolean {
        return oldSubTask.completed != newSubtask.completed
    }
}