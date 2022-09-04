package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.subtasks.domain.entity.SimpleSubtask
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository

class CreateTask(
    private val taskRepository: TaskRepository,
    private val subtaskRepository: SubtaskRepository
) {
    @Throws(ResourceException::class)
    suspend operator fun invoke(taskWithSubtasks: TaskWithSubtasks): Int {
        val (task, subtasks) = taskWithSubtasks

        if (task.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (task.date == null && task.time != null) {
            throw ResourceException.InvalidStateException(
                message = "Date must not be null if time is set"
            )
        }
        val taskWithPosition = setupTaskPosition(task, taskRepository)
        return taskRepository.create(taskWithPosition).toInt().also {
            if (subtasks.isEmpty()) return@also
            subtaskRepository.createMany(*setupSubtaskPositions(it, subtasks))
        }
    }

    private suspend fun setupTaskPosition(task: Task, repository: TaskRepository): Task {
        return repository.findTasksByBoardAmount(task.boardId).let {
            task.copy(position = it)
        }
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
}