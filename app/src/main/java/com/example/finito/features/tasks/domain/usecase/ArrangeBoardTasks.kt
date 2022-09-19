package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository

class ArrangeBoardTasks(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {

    suspend operator fun invoke(tasksWithSubtasks: List<TaskWithSubtasks>): Result<Unit, String> {
        val tasks = tasksWithSubtasks.map { it.task }
        val subtasks = tasksWithSubtasks.flatMap { it.subtasks }

        if (!fromSameBoard(tasks)) {
            return Result.Error(message = ErrorMessages.DIFFERENT_TASKS_ORIGIN)
        }
        return Result.Success(
            data = tasksWithSubtasks.mapIndexed { index, taskWithSubtasks ->
                taskWithSubtasks.task.copy(boardPosition = index)
            }.toTypedArray().let { taskRepository.updateMany(*it) }.also {
                arrangeSubtasks(subtasks)
            }
        )
    }

    private fun fromSameBoard(tasks: List<Task>): Boolean {
        if (tasks.isEmpty()) return true

        val boardId = tasks[0].boardId
        return tasks.all { it.boardId == boardId }
    }

    private suspend fun arrangeSubtasks(subtasks: List<Subtask>) {
        if (subtasks.isEmpty()) return
        val positionsMap = mutableMapOf<Int, Int>()
        subtasks.map {
            positionsMap[it.taskId] =
                if (positionsMap[it.taskId] == null) 0 else positionsMap[it.taskId]!! + 1
            it.copy(position = positionsMap[it.taskId]!!)
        }.let { subtaskRepository.updateMany(*it.toTypedArray()) }
    }
}