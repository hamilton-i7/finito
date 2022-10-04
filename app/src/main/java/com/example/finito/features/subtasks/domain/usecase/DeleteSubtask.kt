package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class DeleteSubtask(
    private val repository: SubtaskRepository
) {

    suspend operator fun invoke(vararg subtasks: Subtask): Result<Unit, String> {
        if (subtasks.any { !isValidId(it.subtaskId) }) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        return Result.Success(
            data = repository.removeMany(*subtasks).also {
                with(mutableListOf<Subtask>()) {
                    subtasks.groupBy { it.taskId }.keys.forEach {
                        addAll(repository.findAllByTaskId(it))
                    }
                    arrangeSubtasks(subtasks = this)
                }
            }
        )
    }

    private suspend fun arrangeSubtasks(subtasks: List<Subtask>) {
        with(mutableMapOf<Int, Int>()) {
            subtasks.map {
                this[it.taskId] =
                    if (this[it.taskId] == null) 0 else this[it.taskId]!! + 1
                it.copy(position = this[it.taskId])
            }.let { repository.updateMany(*it.toTypedArray()) }
        }
    }
}