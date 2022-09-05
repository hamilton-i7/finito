package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository

class ArrangeBoardTasks(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {
    @Throws(ResourceException.InvalidStateException::class)
    suspend operator fun invoke(tasksWithSubtasks: List<TaskWithSubtasks>) {
        val tasks = tasksWithSubtasks.map { it.task }
        val subtasks = tasksWithSubtasks.flatMap { it.subtasks }

        if (!fromSameBoard(tasks)) {
            throw ResourceException.InvalidStateException(
                message = "All tasks must come from the same board"
            )
        }
        if (!fromSameTask(subtasks)) {
            throw ResourceException.InvalidStateException(
                message = "All subtasks must come from the same task"
            )
        }
        tasksWithSubtasks.mapIndexed { index, taskWithSubtasks ->
            taskWithSubtasks.task.copy(boardPosition = index)
        }.toTypedArray().let { taskRepository.updateMany(*it) }
        arrangeSubtasks(subtasks)
    }

    private fun fromSameBoard(tasks: List<Task>): Boolean {
        if (tasks.isEmpty()) return true

        val boardId = tasks[0].boardId
        return tasks.all { it.boardId == boardId }
    }

    private fun fromSameTask(subtasks: List<Subtask>): Boolean {
        if (subtasks.isEmpty()) return true

        val taskId = subtasks[0].taskId
        return subtasks.all { it.taskId == taskId }
    }

    private suspend fun arrangeSubtasks(subtasks: List<Subtask>) {
        val positionsMap = mutableMapOf<Int, Int>()
        subtasks.map {
            positionsMap[it.taskId] =
                if (positionsMap[it.taskId] == null) 0 else positionsMap[it.taskId]!! + 1
            it.copy(position = positionsMap[it.taskId]!!)
        }.let { subtaskRepository.updateMany(*it.toTypedArray()) }
    }
}