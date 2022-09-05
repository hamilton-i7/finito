package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.tasks.domain.util.TaskOrder
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FindTomorrowTasks(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository
) {
    operator fun invoke(
        taskOrder: TaskOrder? = null
    ): Flow<List<TaskWithSubtasks>> {
        return taskRepository.findTomorrowTasks().map { tasks ->
            val activeBoardIds = boardRepository.findActiveBoards().first().groupBy {
                it.board.boardId
            }
            val filteredTasks = tasks.filter { activeBoardIds[it.task.boardId] != null }
            when (taskOrder) {
                TaskOrder.MOST_URGENT -> filteredTasks.sortedWith(
                    compareByDescending<TaskWithSubtasks> {
                        it.task.priority?.level
                    }.thenByDescending { it.task.time }
                )
                TaskOrder.LEAST_URGENT -> filteredTasks.sortedWith(
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