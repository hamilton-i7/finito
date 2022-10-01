package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted

class FindOneBoard(
    private val repository: BoardRepository
) {

    suspend operator fun invoke(id: Int): Result<DetailedBoard, String> {
        if (!isValidId(id)) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        return repository.findOne(id)?.let { detailedBoard ->
            Result.Success(
                data = detailedBoard.copy(
                    tasks = detailedBoard.tasks.let { tasks ->
                        val uncompletedTasks = tasks.filterUncompleted().sortedBy {
                            it.task.boardPosition
                        }.let { list ->
                            list.map { it.copy(
                                subtasks = it.subtasks.sortedBy { subtask -> subtask.position }
                            ) }
                        }
                        val completedTasks = tasks.filterCompleted().sortedBy { it.task.completedAt }
                        uncompletedTasks + completedTasks
                    }
                )
            )
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
    }
}