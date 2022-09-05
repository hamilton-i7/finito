package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.moveElement
import com.example.finito.features.subtasks.domain.entity.SimpleSubtask
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
    @Throws(ResourceException::class)
    suspend operator fun invoke(taskWithSubtasks: TaskWithSubtasks) {
        val (task, subtasks) = taskWithSubtasks
        
        if (task.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (task.date == null && task.time != null) {
            throw ResourceException.InvalidStateException(
                message = "Date must not be null if time is set"
            )
        }
        taskRepository.findOne(task.taskId)?.let {
            if (changedBoard(it.task, task)) {
                arrangeDiffBoard(
                    startBoardId = it.task.boardId,
                    endBoardId = task.boardId,
                    taskToUpdate = task,
                    repository = taskRepository,
                )
            } else if (changedPosition(it.task, task)) {
                arrangeSameBoard(
                    boardId = task.boardId,
                    from = it.task.position,
                    to = task.position,
                    repository = taskRepository
                )
            }
        } ?: throw ResourceException.NotFoundException
        return taskRepository.update(task.toTaskUpdate()).also {
            val oldSubtasks = subtaskRepository.findAllByTaskId(task.taskId).toTypedArray()
            with(setupSubtaskPositions(task.taskId, subtasks)) {
                // Delete subtasks not found in the old subtasks list
                deleteSubtasks(oldSubtasks, newSubtasks = this, subtaskRepository)
                // Create the new subtasks
                createSubtasks(subtasks = this, subtaskRepository)

                filter { it.subtaskId != 0 }.let {
                    val ids = oldSubtasks.groupBy { subtask -> subtask.subtaskId }
                    if (it.any { subtask -> ids[subtask.subtaskId] == null }) {
                        throw ResourceException.NotFoundException
                    }
                    subtaskRepository.updateMany(*it.toTypedArray())
                }
            }
        }
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
        taskToUpdate: Task,
        repository: TaskRepository
    ) {
        val startBoardTasks = repository
            .findTasksByBoard(startBoardId)
            .filter { it.taskId != taskToUpdate.taskId }
            .mapIndexed { index, task ->
                task.copy(position = index)
            }.toTypedArray()
        repository.updateMany(*startBoardTasks)

        val endBoardTasks = repository
            .findTasksByBoard(endBoardId)
            .toMutableList()
            .also {  it.add(taskToUpdate) }
            .mapIndexed { index, task ->
                task.copy(position = index)
            }.toTypedArray()
        repository.updateMany(*endBoardTasks)
    }

    private fun setupSubtaskPositions(taskId: Int, subtasks: List<SimpleSubtask>): Array<Subtask> {
        return subtasks.mapIndexed { index, subtask ->
            Subtask(
                taskId = taskId,
                name = subtask.name,
                position = index
            )
        }.toTypedArray()
    }

    private suspend fun createSubtasks(
        subtasks: Array<out Subtask>,
        repository: SubtaskRepository,
    ) {
        subtasks.filter { it.subtaskId == 0 }.let {
            repository.createMany(*it.toTypedArray())
        }
    }

    private suspend fun deleteSubtasks(
        oldSubtasks: Array<out Subtask>,
        newSubtasks: Array<out Subtask>,
        repository: SubtaskRepository
    ) {
        val ids = newSubtasks.groupBy { it.subtaskId }
        oldSubtasks.filter { ids[it.subtaskId] == null }.let {
            repository.removeMany(*it.toTypedArray())
        }
    }

}