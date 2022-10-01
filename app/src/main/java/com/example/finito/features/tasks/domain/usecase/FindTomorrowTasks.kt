package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FindTomorrowTasks(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository
) {
    operator fun invoke(
        taskOrder: SortingOption.Priority? = null
    ): Result.Success<Flow<List<TaskWithSubtasks>>> {
        return Result.Success(
            data = taskRepository.findTomorrowTasks().map { tasks ->
                val activeBoardIds = boardRepository.findActiveBoards().first().groupBy {
                    it.board.boardId
                }
                val filteredTasks = tasks.filter { activeBoardIds[it.task.boardId] != null }
                val uncompletedTasks = filteredTasks.filterUncompleted().let { uncompleted ->
                    when (taskOrder) {
                        SortingOption.Priority.MostUrgent -> uncompleted.sortedWith(
                            compareByDescending<TaskWithSubtasks> {
                                it.task.priority?.level
                            }.thenBy { it.task.time }
                        )
                        SortingOption.Priority.LeastUrgent -> uncompleted.sortedWith(
                            compareBy<TaskWithSubtasks> {
                                it.task.priority?.level
                            }.thenBy { it.task.time }
                        )
                        null -> uncompleted.sortedBy { it.task.time }
                    }
                }
                val completedTasks = filteredTasks.filterCompleted().sortedBy { it.task.completedAt }
                uncompletedTasks + completedTasks
            }
        )
    }
}