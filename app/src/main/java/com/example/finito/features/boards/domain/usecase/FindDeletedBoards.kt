package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindDeletedBoards(
    private val repository: BoardRepository
) {
    operator fun invoke(): Result.Success<Flow<List<BoardWithLabelsAndTasks>>> {
        return Result.Success(
            data = repository.findDeletedBoards().map { boards ->
                boards.sortedByDescending { it.board.removedAt }
            }
        )
    }
}