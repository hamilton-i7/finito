package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.moveElement
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.filterUncompleted
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class CreateSubtask(private val repository: SubtaskRepository) {

    suspend operator fun invoke(subtask: Subtask): Result<Unit, String> {
        if (subtask.name.isBlank()) {
            return Result.Error(message = ErrorMessages.EMPTY_NAME)
        }
        val subtaskWithPosition = subtask.let {
            if (it.position == null && !it.completed) {
                return@let setupSubtaskPosition(it)
            }
            if (it.position == null) return@let it
            arrangeSubtasks(subtask = it)
        }
        return Result.Success(
            data = repository.createMany(subtaskWithPosition.copy(
                name = subtaskWithPosition.name.trim(),
                description = subtaskWithPosition.description?.trim()
            ))
        )
    }

    private suspend fun setupSubtaskPosition(subtask: Subtask): Subtask {
        return repository.findAllByTaskId(subtask.taskId).let {
            subtask.copy(position = it.filterUncompleted().size)
        }
    }

    private suspend fun arrangeSubtasks(subtask: Subtask): Subtask {
        val subtasks = repository.findAllByTaskId(subtask.taskId) + listOf(subtask)
        val arrangedSubtasks = subtasks.moveElement(subtasks.lastIndex, subtask.position!!).mapIndexed { index, it ->
            it.copy(position = index)
        }.toTypedArray()
        repository.updateMany(*arrangedSubtasks)
        return arrangedSubtasks.first { it.taskId == subtask.taskId }
    }
}