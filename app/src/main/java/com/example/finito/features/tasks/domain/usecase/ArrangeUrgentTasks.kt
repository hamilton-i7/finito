package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import com.example.finito.features.tasks.domain.util.Priority

class ArrangeUrgentTasks(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {
    suspend operator fun invoke(tasksWithSubtasks: List<TaskWithSubtasks>): Result<Unit, String> {
        val tasks = tasksWithSubtasks.filter { it.task.completed }.map { it.task }
        val subtasks = tasksWithSubtasks.flatMap { it.subtasks }

        if (!areUrgent(tasks)) {
            return Result.Error(message = "All tasks must have urgent priority")
        }
        taskRepository.updateMany(*tasks.toTypedArray())
        return Result.Success(data = arrangeSubtasks(subtasks))
    }

    private fun areUrgent(tasks: List<Task>): Boolean {
        if (tasks.isEmpty()) return true
        return tasks.all { it.priority == Priority.URGENT }
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