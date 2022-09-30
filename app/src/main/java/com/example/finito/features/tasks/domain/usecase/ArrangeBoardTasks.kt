package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.filterUncompleted
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.domain.repository.TaskRepository

class ArrangeBoardTasks(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {

    suspend operator fun invoke(tasksWithSubtasks: List<TaskWithSubtasks>): Result<Unit, String> {
        val uncompletedTasks = tasksWithSubtasks.filterUncompleted()
        val subtasks = uncompletedTasks.flatMap { it.subtasks }.filterUncompleted()

        if (!fromSameBoard(uncompletedTasks.map { it.task })) {
            return Result.Error(message = ErrorMessages.DIFFERENT_TASKS_ORIGIN)
        }
        return Result.Success(
            data = uncompletedTasks.mapIndexed { index, task ->
                task.task.copy(boardPosition = index)
            }.toTypedArray().let {
                val newTasks = it.filter { task -> task.taskId == 0 }.also(::println)
                val tasksToUpdate = it.filter { task -> task.taskId != 0 }.also(::println).toTypedArray()

                newTasks.forEach { task -> taskRepository.create(task) }
                taskRepository.updateMany(*tasksToUpdate)
            }.also { arrangeSubtasks(subtasks) }
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
        }.let {
            val newSubtasks = it.filter { subtask -> subtask.subtaskId == 0 }
            val subtasksToUpdate = it.filter { subtask -> subtask.subtaskId != 0 }
            subtaskRepository.createMany(*newSubtasks.toTypedArray())
            subtaskRepository.updateMany(*subtasksToUpdate.toTypedArray())
        }
    }
}