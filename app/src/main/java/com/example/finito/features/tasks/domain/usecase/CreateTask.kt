package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.repository.TaskRepository

class CreateTask(
    private val repository: TaskRepository
) {
    @Throws(ResourceException::class)
    suspend operator fun invoke(task: Task) {
        if (task.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (task.date == null && task.time != null) {
            throw ResourceException.InvalidStateException(
                message = "Date must not be null if time is set"
            )
        }
        val taskWithPosition = setupTaskPosition(task, repository)
        return repository.create(taskWithPosition)
    }

    private suspend fun setupTaskPosition(task: Task, repository: TaskRepository): Task {
        return repository.findTasksByBoardAmount(task.boardId).let {
            task.copy(position = it)
        }
    }
}