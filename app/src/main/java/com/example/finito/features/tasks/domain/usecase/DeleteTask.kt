package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.isValidId
import com.example.finito.core.util.moveElement
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlin.jvm.Throws

class DeleteTask(
    private val repository: TaskRepository
) {
    @Throws(InvalidIdException::class)
    suspend operator fun invoke(task: Task) {
        if (!isValidId(task.taskId)) {
            throw InvalidIdException
        }
        arrangeTasks(task, repository)
        return repository.remove(task)
    }

    private suspend fun arrangeTasks(
        task: Task,
        repository: TaskRepository,
    ) {
        with(repository.findTasksByBoard(task.boardId).toMutableList()) {
            removeIf { it.taskId == task.taskId }
            mapIndexed { index, task ->
                task.copy(position = index)
            }.toTypedArray().also {
                repository.updateMany(*it)
            }
        }
    }
}