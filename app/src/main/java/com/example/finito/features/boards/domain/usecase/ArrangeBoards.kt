package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.repository.BoardRepository

class ArrangeBoards(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(boards: List<BoardWithLabelsAndTasks>): Result.Success<Unit> {
        return Result.Success(
            data = boards.mapIndexed { index, boardWithLabelsAndTasks ->
                boardWithLabelsAndTasks.board.copy(position = index)
            }.let { repository.update(*it.toTypedArray()) }
        )
    }
}