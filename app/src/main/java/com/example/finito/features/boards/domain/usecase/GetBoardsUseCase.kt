package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.boards.domain.util.BoardOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBoardsUseCase(
    private val repository: BoardRepository
) {
    operator fun invoke(
        boardOrder: BoardOrder = BoardOrder.A_Z
    ): Flow<List<BoardWithLabels>> {
        return repository.getBoards().map { boards ->
            when(boardOrder) {
                BoardOrder.A_Z -> boards.sortedBy { it.board.name.lowercase() }
                BoardOrder.Z_A -> boards.sortedByDescending { it.board.name.lowercase() }
                BoardOrder.NEWEST -> boards.sortedBy { it.board.createdAt }
                BoardOrder.OLDEST -> boards.sortedByDescending { it.board.createdAt }
            }
        }
    }
}