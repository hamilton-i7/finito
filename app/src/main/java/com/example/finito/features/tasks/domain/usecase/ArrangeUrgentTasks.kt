package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.Priority
import com.example.finito.core.util.ResourceException
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first

class ArrangeUrgentTasks(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {
    @Throws(ResourceException.InvalidStateException::class)
    suspend operator fun invoke(tasksWithSubtasks: List<TaskWithSubtasks>) {
        val tasks = tasksWithSubtasks.filter { it.task.completed }.map { it.task }
        val subtasks = tasksWithSubtasks.flatMap { it.subtasks }

        if (!areUrgent(tasks)) {
            throw ResourceException.InvalidStateException(
                message = "All tasks must have urgent priority"
            )
        }

        // Set old urgent tasks to default value
        taskRepository.findTodayTasks().first().map {
            it.task.copy(urgentPosition = -1)
        }.toTypedArray().let { taskRepository.updateMany(*it) }

        tasks.mapIndexed { index, task ->
            task.copy(urgentPosition = index)
        }.toTypedArray().let { taskRepository.updateMany(*it) }
        arrangeSubtasks(subtasks)
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