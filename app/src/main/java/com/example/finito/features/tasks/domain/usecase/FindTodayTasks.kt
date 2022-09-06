package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FindTodayTasks(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository
) {
    operator fun invoke(
        taskOrder: SortingOption.Priority? = null
    ): Flow<List<TaskWithSubtasks>> {
        return taskRepository.findTodayTasks().map { tasks ->
            val activeBoardIds = boardRepository.findActiveBoards().first().groupBy {
                it.board.boardId
            }
            val filteredTasks = tasks.filter { activeBoardIds[it.task.boardId] != null }
            when (taskOrder) {
                SortingOption.Priority.MostUrgent -> filteredTasks.sortedWith(
                    compareByDescending<TaskWithSubtasks> {
                        it.task.priority?.level
                    }.thenByDescending { it.task.time }
                )
                SortingOption.Priority.LeastUrgent -> filteredTasks.sortedWith(
                    compareBy<TaskWithSubtasks> {
                        it.task.priority?.level
                    }.thenByDescending { it.task.time }
                )
                null -> filteredTasks.sortedWith(
                    compareByDescending { it.task.time }
                )
            }
        }
    }
}