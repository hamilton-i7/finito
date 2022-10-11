package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import java.time.LocalDateTime

class ToggleTaskCompleted(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {
    suspend operator fun invoke(
        taskWithSubtasks: TaskWithSubtasks,
        undoingToggle: Boolean = false,
    ): Result<Unit, String> {
        val (task, subtasks) = taskWithSubtasks
        val tasks = taskRepository.findTasksByBoard(task.boardId)

        if (task.name.isBlank()) {
            return Result.Error(ErrorMessages.EMPTY_NAME)
        }
        if (task.date == null && task.time != null) {
            return Result.Error(ErrorMessages.INVALID_TASK_STATE)
        }
        if (!fromSameTask(subtasks)) {
            return Result.Error(ErrorMessages.DIFFERENT_SUBTASKS_ORIGIN)
        }

        val positionedTask = taskRepository.findOne(task.taskId)?.run {
            if (task.completed) {
                markSubtasksAsCompleted(subtasks)
                arrangeTasks(tasks.filter { it.taskId != task.taskId && !it.completed })
                return@run task.copy(boardPosition = null)
            }
            markSubtasksAsUncompleted(subtasks)
            return@run task.copy(boardPosition = if (undoingToggle) {
                arrangeTasks(
                    tasks = tasks.filter { !it.completed }.toMutableList().apply {
                        add(index = task.boardPosition!!, element = task)
                    }
                )
                task.boardPosition
            } else {
                getBoardPosition(
                    tasks = tasks.filter { it.taskId != task.taskId && !it.completed } + listOf(task)
                )
            })
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
        return Result.Success(data = taskRepository.update(positionedTask))
    }

    private fun fromSameTask(subtasks: List<Subtask>): Boolean {
        if (subtasks.isEmpty()) return true

        val taskId = subtasks[0].taskId
        return subtasks.all { it.taskId == taskId }
    }

    private suspend fun markSubtasksAsUncompleted(subtasks: List<Subtask>) {
        subtaskRepository.updateMany(
            *subtasks.mapIndexed { index, subtask ->
                subtask.copy(
                    position = index,
                    completed = false,
                    completedAt = null
                )
            }.toTypedArray()
        )
    }

    private suspend fun markSubtasksAsCompleted(subtasks: List<Subtask>) {
        subtaskRepository.updateMany(
            *subtasks.mapIndexed { index, subtask ->
                subtask.copy(
                    position = null,
                    completed = true,
                    completedAt = subtask.completedAt ?: LocalDateTime.now().plusSeconds(index.toLong())
                )
            }.toTypedArray()
        )
    }

    private suspend fun getBoardPosition(tasks: List<Task>): Int {
        return tasks.also { arrangeTasks(it) }.lastIndex
    }

    private suspend fun arrangeTasks(tasks: List<Task>) {
        tasks.mapIndexed { index, task -> task.copy(boardPosition = index) }.let { uncompletedTasks ->
            taskRepository.updateMany(*uncompletedTasks.toTypedArray())
        }
    }
}