package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.SubtaskRepository
import kotlin.jvm.Throws

class CreateManySubtasks(
    private val repository: SubtaskRepository
) {
    @Throws(ResourceException.EmptyException::class)
    suspend operator fun invoke(vararg subtasks: Subtask) {
        if (subtasks.any { it.name.isBlank() }) {
            throw ResourceException.EmptyException
        }
        return repository.createMany(*setupPositions(subtasks))
    }

    private fun setupPositions(subtasks: Array<out Subtask>): Array<Subtask> {
        return subtasks.mapIndexed { index, subtask ->
            subtask.copy(position = index)
        }.toTypedArray()
    }
}