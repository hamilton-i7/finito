package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.SortingOptions
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindAllBoards(
    private val repository: BoardRepository
) {
    operator fun invoke(
        boardOrder: SortingOptions.Common = SortingOptions.Common.A_Z,
        vararg labelIds: Int,
    ): Flow<List<BoardWithLabelsAndTasks>> {
        return repository.findActiveBoards().map { boards ->
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
        boardOrder: SortingOptions.Common,
        boards: List<BoardWithLabelsAndTasks>,
    ): List<BoardWithLabelsAndTasks> = when (boardOrder) {
        SortingOptions.Common.A_Z -> boards.sortedBy { it.board.normalizedName }
        SortingOptions.Common.Z_A -> boards.sortedByDescending { it.board.normalizedName }
        SortingOptions.Common.Newest -> boards.sortedByDescending { it.board.createdAt }
        SortingOptions.Common.Oldest -> boards.sortedBy { it.board.createdAt }
    }
}