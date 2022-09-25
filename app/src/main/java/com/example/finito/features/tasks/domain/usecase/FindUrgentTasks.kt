package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FindUrgentTasks(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository
) {
    operator fun invoke(): Result.Success<Flow<Map<LocalDate?, List<TaskWithSubtasks>>>> {
        return Result.Success(
            data = taskRepository.findUrgentTasks().map { tasks ->
                val activeBoardIds = boardRepository.findActiveBoards().first().groupBy {
                    it.board.boardId
                }
                val tasksFromActiveBoards = tasks.filter { activeBoardIds[it.task.boardId] != null }
                val uncompletedTasks = tasksFromActiveBoards.filterUncompleted().let { uncompleted ->
                    uncompleted.sortedWith(
                        compareByDescending<TaskWithSubtasks> {
                            it.task.date
                        }.thenBy { it.task.time }
                    )
                }
                val completedTasks = tasksFromActiveBoards.filterCompleted().sortedBy { it.task.completedAt }
                val sortedTasks = uncompletedTasks + completedTasks
                sortedTasks.groupBy { it.task.date }
            }
        )
    }
}