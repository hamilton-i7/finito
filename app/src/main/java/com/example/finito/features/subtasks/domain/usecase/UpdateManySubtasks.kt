package com.example.finito.features.subtasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.SubtaskRepository
import kotlin.jvm.Throws

class UpdateManySubtasks(
    private val repository: SubtaskRepository
) {
    @Throws(
        ResourceException.EmptyException::class,
        ResourceException.NegativeIdException::class,
        ResourceException.InvalidStateException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(vararg subtasks: Subtask): Int {
        if (subtasks.any { it.name.isBlank() }) {
            throw ResourceException.EmptyException
        }
        if (subtasks.any { !isValidId(it.subtaskId) }) {
            throw ResourceException.NegativeIdException
        }
        if (!fromSameTask(subtasks)) {
            throw ResourceException.InvalidStateException(
                message = "All subtasks must come from the same task"
            )
        }

        val oldSubtasks = repository.findAllByTaskId(subtasks[0].taskId).toTypedArray()
        with(setupPositions(subtasks)) {
            // Delete subtasks not found in the old subtasks list
            deleteSubtasks(oldSubtasks, newSubtasks = this, repository)
            // Create the new subtasks
            createSubtasks(subtasks = this, repository)

            return this.filter { it.subtaskId != 0 }.let {
                val updatedSubtasksAmount = repository.updateMany(*it.toTypedArray())
                if (updatedSubtasksAmount != it.size) throw ResourceException.NotFoundException
                else updatedSubtasksAmount
            }
        }
    }

    private fun fromSameTask(subtasks: Array<out Subtask>): Boolean {
        if (subtasks.isEmpty()) return true
        return subtasks[0].taskId.let { id ->
            subtasks.all { it.taskId == id }
        }
    }

    private fun setupPositions(subtasks: Array<out Subtask>): Array<Subtask> {
        return subtasks.mapIndexed { index, subtask ->
            subtask.copy(position = index)
        }.toTypedArray()
    }

    private suspend fun createSubtasks(
        subtasks: Array<out Subtask>,
        repository: SubtaskRepository,
    ) {
        subtasks.filter { it.subtaskId == 0 }.let {
            repository.createMany(*it.toTypedArray())
        }
    }

    private suspend fun deleteSubtasks(
        oldSubtasks: Array<out Subtask>,
        newSubtasks: Array<out Subtask>,
        repository: SubtaskRepository
    ) {
        val ids = newSubtasks.groupBy { it.subtaskId }
        oldSubtasks.filter { ids[it.subtaskId] == null }.let {
            val deletedSubtasksAmount = repository.removeMany(*it.toTypedArray())
            if (deletedSubtasksAmount != it.size) throw ResourceException.NotFoundException
        }
    }
}