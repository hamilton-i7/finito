package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.moveElement
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.toTaskUpdate
import com.example.finito.features.tasks.domain.repository.TaskRepository

class UpdateTask(
    private val repository: TaskRepository
) {
    @Throws(ResourceException::class)
    suspend operator fun invoke(task: Task) {
        if (task.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (task.date == null && task.time != null) {
            throw ResourceException.InvalidException(
                message = "Date must not be null if time is set"
            )
        }
        val oldTask = repository.findOne(task.taskId) ?: throw ResourceException.NotFound
        if (changedBoard(oldTask, task)) {
            arrangeDiffBoard(
                startBoardId = oldTask.boardId,
                endBoardId = task.boardId,
                newTask = task,
                repository = repository,
            )
        } else if (changedPosition(oldTask, task)) {
            arrangeSameBoard(
                boardId = task.boardId,
                from = oldTask.position,
                to = task.position,
                repository = repository
            )
        }
        return repository.update(task.toTaskUpdate())
    }

    private fun changedPosition(oldTask: Task, newTask: Task): Boolean {
        return oldTask.position != newTask.position
    }

    private fun changedBoard(oldTask: Task, newTask: Task): Boolean {
        return oldTask.boardId != newTask.boardId
    }

    private suspend fun arrangeSameBoard(
        boardId: Int,
        from: Int,
        to: Int,
        repository: TaskRepository,
    ) {
        val tasks = repository.findTasksByBoard(boardId)
        val arrangedTasks = tasks.moveElement(from, to).mapIndexed { index, task ->
            task.copy(position = index)
        }.toTypedArray()
        repository.updateMany(*arrangedTasks)
    }

    private suspend fun arrangeDiffBoard(
        startBoardId: Int,
        endBoardId: Int,
        newTask: Task,
        repository: TaskRepository
    ) {
        val startBoardTasks = repository
            .findTasksByBoard(startBoardId)
            .filter { it.taskId != newTask.taskId }
            .mapIndexed { index, task ->
                task.copy(position = index)
            }.toTypedArray()
        repository.updateMany(*startBoardTasks)

        val endBoardTasks = repository
            .findTasksByBoard(endBoardId)
            .toMutableList()
            .also {  it.add(newTask) }
            .mapIndexed { index, task ->
                task.copy(position = index)
            }.toTypedArray()
        repository.updateMany(*endBoardTasks)
    }
}