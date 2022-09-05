package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FindUrgentTasks(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository
) {
    operator fun invoke(): Flow<Map<LocalDate?, List<TaskWithSubtasks>>> {
        return taskRepository.findUrgentTasks().map { tasks ->
            val activeBoardIds = boardRepository.findActiveBoards().first().groupBy {
                it.board.boardId
            }
            val filteredTasks = tasks.filter { activeBoardIds[it.task.boardId] != null }
            val sortedTasks = filteredTasks.sortedWith(
                compareByDescending<TaskWithSubtasks> {
                    it.task.date
                }.thenByDescending { it.task.time }
            )
            sortedTasks.groupBy { it.task.date }
        }
    }
}