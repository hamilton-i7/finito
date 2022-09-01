package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.boards.domain.util.BoardOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindDeletedBoards(
    private val repository: BoardRepository
) {
    operator fun invoke(
        boardOrder: BoardOrder = BoardOrder.A_Z,
        vararg labelIds: Int,
    ): Flow<List<BoardWithLabels>> {
        return repository.findDeletedBoards().map { boards ->
            if (labelIds.isEmpty()) {
                return@map sortBoards(boardOrder, boards)
            }
            val idsMap = labelIds.groupBy { it }
            val filteredBoards = boards.filter { board ->
                board.labels.any { idsMap[it.labelId] != null }
            }
            sortBoards(boardOrder, filteredBoards)
        }
    }

    private fun sortBoards(
        boardOrder: BoardOrder,
        boards: List<BoardWithLabels>,
    ): List<BoardWithLabels> = when(boardOrder) {
        BoardOrder.A_Z -> boards.sortedBy { it.board.normalizedName }
        BoardOrder.Z_A -> boards.sortedByDescending { it.board.normalizedName }
        BoardOrder.NEWEST -> boards.sortedByDescending { it.board.createdAt }
        BoardOrder.OLDEST -> boards.sortedBy { it.board.createdAt }
    }
}