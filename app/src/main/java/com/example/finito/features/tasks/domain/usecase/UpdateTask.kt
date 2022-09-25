package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.ResourceException
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository

class UpdateTask(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository,
) {

    suspend operator fun invoke(task: Task): Result<Unit, String> {
        if (task.name.isBlank()) {
            return Result.Error(ErrorMessages.EMPTY_NAME)
        }
        if (task.date == null && task.time != null) {
            return Result.Error(ErrorMessages.INVALID_TASK_STATE)
        }

        val positionedTask = taskRepository.findOne(task.taskId)?.let {
            if (changedBoard(it.task, task)) {
                return@let arrangeDiffBoard(
                    startBoardId = it.task.boardId,
                    endBoardId = task.boardId,
                    taskToUpdate = task,
                )
            }
            if (!changedCompletedState(it.task, task)) return@let task
            if (task.completed) {
                return@let task.copy(boardPosition = null)
            }
            return@let task.copy(
                boardPosition = taskRepository.findTasksByBoard(task.boardId).filter { task1 ->
                    !task1.completed
                }.size
            )
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
        return Result.Success(
            data = taskRepository.update(positionedTask)
        )
    }

    suspend operator fun invoke(tasksWithSubtasks: TaskWithSubtasks): Result<Unit, String> {
        val (task, subtasks) = tasksWithSubtasks
        
        if (task.name.isBlank()) {
            return Result.Error(ErrorMessages.EMPTY_NAME)
        }
        if (task.date == null && task.time != null) {
            return Result.Error(ErrorMessages.INVALID_TASK_STATE)
        }
        if (!fromSameTask(subtasks)) {
            return Result.Error(ErrorMessages.DIFFERENT_SUBTASKS_ORIGIN)
        }

        val positionedTask = taskRepository.findOne(task.taskId)?.let {
            if (changedBoard(it.task, task)) {
                return@let arrangeDiffBoard(
                    startBoardId = it.task.boardId,
                    endBoardId = task.boardId,
                    taskToUpdate = task,
                )
            }
            if (!changedCompletedState(it.task, task)) return@let task
            if (task.completed) {
                return@let task.copy(boardPosition = null)
            }
            return@let task.copy(
                boardPosition = taskRepository.findTasksByBoard(task.boardId).filter { task1 ->
                    !task1.completed
                }.size
            )
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
        return Result.Success(
            data = taskRepository.update(positionedTask).also {
                val oldSubtasks = subtaskRepository.findAllByTaskId(task.taskId).toTypedArray()
                with(setupSubtaskPositions(subtasks)) {
                    // Delete subtasks not found in the old subtasks list
                    deleteSubtasks(oldSubtasks, newSubtasks = this)
                    // Create the new subtasks
                    createSubtasks(subtasks = this)

                    filter { it.subtaskId != 0 }.let {
                        val ids = oldSubtasks.groupBy { subtask -> subtask.subtaskId }
                        if (it.any { subtask -> ids[subtask.subtaskId] == null }) {
                            throw ResourceException.NotFoundException
                        }
                        subtaskRepository.updateMany(*it.toTypedArray())
                    }
                }
            }
        )
    }

    private fun changedCompletedState(oldTask: Task, newTask: Task): Boolean {
        return oldTask.completed != newTask.completed
    }

    private fun changedBoard(oldTask: Task, newTask: Task): Boolean {
        return oldTask.boardId != newTask.boardId
    }

    private fun fromSameTask(subtasks: List<Subtask>): Boolean {
        if (subtasks.isEmpty()) return true

        val taskId = subtasks[0].taskId
        return subtasks.all { it.taskId == taskId }
    }

    private suspend fun arrangeDiffBoard(
        startBoardId: Int,
        endBoardId: Int,
        taskToUpdate: Task,
    ): Task {
        var newPosition = 0

        val startBoardTasks = taskRepository
            .findTasksByBoard(startBoardId)
            .filter { it.taskId != taskToUpdate.taskId && !it.completed }
            .mapIndexed { index, task ->
                if (task.taskId == taskToUpdate.taskId) {
                    newPosition = index
                }
                task.copy(boardPosition = index)
            }.toTypedArray()
        taskRepository.updateMany(*startBoardTasks)

        val endBoardTasks = taskRepository
            .findTasksByBoard(endBoardId)
            .filter { !it.completed }
            .toMutableList()
            .apply {  add(taskToUpdate) }
            .mapIndexed { index, task ->
                task.copy(boardPosition = index)
            }.toTypedArray()
        taskRepository.updateMany(*endBoardTasks)
        return taskToUpdate.copy(boardPosition = newPosition)
    }

    private fun setupSubtaskPositions(subtasks: List<Subtask>): Array<Subtask> {
        return subtasks.mapIndexed { index, subtask ->
            subtask.copy(position = index)
        }.toTypedArray()
    }

    private suspend fun createSubtasks(subtasks: Array<out Subtask>) {
        subtasks.filter { it.subtaskId == 0 }.let {
            subtaskRepository.createMany(*it.toTypedArray())
        }
    }

    private suspend fun deleteSubtasks(
        oldSubtasks: Array<out Subtask>,
        newSubtasks: Array<out Subtask>,
    ) {
        val ids = newSubtasks.groupBy { it.subtaskId }
        oldSubtasks.filter { ids[it.subtaskId] == null }.let {
            subtaskRepository.removeMany(*it.toTypedArray())
        }
    }

}