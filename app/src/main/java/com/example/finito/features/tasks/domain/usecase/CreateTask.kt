package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository

class CreateTask(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {

    suspend operator fun invoke(taskWithSubtasks: TaskWithSubtasks): Result<Int, String> {
        val (task, subtasks) = taskWithSubtasks

        if (task.name.isBlank()) {
            return Result.Error(message = ErrorMessages.EMPTY_NAME)
        }
        if (task.date == null && task.time != null) {
            return Result.Error(message = ErrorMessages.INVALID_TASK_STATE)
        }
        val taskWithPosition = setupTaskPosition(task)
        return Result.Success(
            data = taskRepository.create(taskWithPosition.copy(
                name = taskWithPosition.name.trim(),
                description = taskWithPosition.description?.trim()
            )
            ).toInt().also {
                if (subtasks.isEmpty()) return@also
                subtaskRepository.createMany(*setupSubtaskPositions(it, subtasks))
            }
        )
    }

    private suspend fun setupTaskPosition(task: Task): Task {
        // If this task was recently deleted, use its board position
        if (task.boardPosition != -1) return task

        return taskRepository.findTasksByBoardAmount(task.boardId).let {
            task.copy(boardPosition = it)
        }
    }

    private fun setupSubtaskPositions(taskId: Int, subtasks: List<Subtask>): Array<Subtask> {
        return subtasks.mapIndexed { index, subtask ->
            subtask.copy(
                position = index,
                taskId = taskId,
                name = subtask.name.trim(),
                description = subtask.description?.trim()
            )
        }.toTypedArray()
    }
}