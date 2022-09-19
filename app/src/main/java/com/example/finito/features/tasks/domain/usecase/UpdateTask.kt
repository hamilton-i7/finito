package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.moveElement
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.toTaskUpdate
import com.example.finito.features.tasks.domain.repository.TaskRepository

class UpdateTask(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository,
) {

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

        taskRepository.findOne(task.taskId)?.let {
            if (changedBoard(it.task, task)) {
                arrangeDiffBoard(
                    startBoardId = it.task.boardId,
                    endBoardId = task.boardId,
                    taskToUpdate = task,
                )
            } else if (changedPosition(it.task, task)) {
                arrangeSameBoard(
                    boardId = task.boardId,
                    from = it.task.boardPosition,
                    to = task.boardPosition,
                )
            }
        } ?: throw ResourceException.NotFoundException
        return Result.Success(
            data = taskRepository.update(task.toTaskUpdate()).also {
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

    private fun changedPosition(oldTask: Task, newTask: Task): Boolean {
        return oldTask.boardPosition != newTask.boardPosition
    }

    private fun changedBoard(oldTask: Task, newTask: Task): Boolean {
        return oldTask.boardId != newTask.boardId
    }

    private fun fromSameTask(subtasks: List<Subtask>): Boolean {
        if (subtasks.isEmpty()) return true

        val taskId = subtasks[0].taskId
        return subtasks.all { it.taskId == taskId }
    }

    private suspend fun arrangeSameBoard(
        boardId: Int,
        from: Int,
        to: Int,
    ) {
        val tasks = taskRepository.findTasksByBoard(boardId)
        val arrangedTasks = tasks.moveElement(from, to).mapIndexed { index, task ->
            task.copy(boardPosition = index)
        }.toTypedArray()
        taskRepository.updateMany(*arrangedTasks)
    }

    private suspend fun arrangeDiffBoard(
        startBoardId: Int,
        endBoardId: Int,
        taskToUpdate: Task,
    ) {
        val startBoardTasks = taskRepository
            .findTasksByBoard(startBoardId)
            .filter { it.taskId != taskToUpdate.taskId }
            .mapIndexed { index, task ->
                task.copy(boardPosition = index)
            }.toTypedArray()
        taskRepository.updateMany(*startBoardTasks)

        val endBoardTasks = taskRepository
            .findTasksByBoard(endBoardId)
            .toMutableList()
            .also {  it.add(taskToUpdate) }
            .mapIndexed { index, task ->
                task.copy(boardPosition = index)
            }.toTypedArray()
        taskRepository.updateMany(*endBoardTasks)
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