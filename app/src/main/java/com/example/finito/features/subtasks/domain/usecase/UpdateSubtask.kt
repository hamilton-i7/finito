package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.filterUncompleted
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
                    position = subtask.position ?: repository
                        .findAllByTaskId(subtask.taskId)
                        .filterUncompleted()
                        .size
                )
            }?.copy(
            name = subtask.name.trim(),
            normalizedName = subtask.normalizedName.trim().normalize()
        ) ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
        return Result.Success(
            data = repository.updateMany(positionedSubtask).also {
                arrangeSubtasks(positionedSubtask)
            }
        )
    }

    private suspend fun arrangeSubtasks(subtask: Subtask) {
        val subtasks = repository.findAllByTaskId(subtask.taskId).filterUncompleted()
        with(subtasks.toMutableList()) {
            subtask.position?.let { position ->
                add(
                    index = position,
                    element = removeAt(indexOfFirst { it.subtaskId == subtask.subtaskId })
                )
            }
            mapIndexed { index, subtask -> subtask.copy(position = index) }.let {
                repository.updateMany(*it.toTypedArray())
            }
        }
    }

    private fun changedCompletedState(oldSubTask: Subtask, newSubtask: Subtask): Boolean {
        return oldSubTask.completed != newSubtask.completed
    }
}