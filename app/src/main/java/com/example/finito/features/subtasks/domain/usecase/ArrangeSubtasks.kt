package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class ArrangeSubtasks(
    private val repository: SubtaskRepository
) {
    suspend operator fun invoke(subtasks: List<Subtask>): Result<Unit, String> {
        val positionedSubtasks = subtasks.mapIndexed { index, subtask ->
            subtask.copy(position = index)
        }.toTypedArray()
        return Result.Success(
            data = repository.updateMany(*positionedSubtasks)
        )
    }
}