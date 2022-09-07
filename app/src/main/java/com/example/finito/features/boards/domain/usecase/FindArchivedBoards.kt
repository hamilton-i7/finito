package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindArchivedBoards(
    private val repository: BoardRepository
) {
    operator fun invoke(
        boardOrder: SortingOption.Common = SortingOption.Common.NameAZ,
        searchQuery: String? = null,
        vararg labelIds: Int,
    ): Flow<List<BoardWithLabelsAndTasks>> {
        return repository.findArchivedBoards().map { boards ->
            val normalizedQuery = searchQuery?.normalize()
            val result = normalizedQuery?.let {
                boards.filter { board -> board.board.normalizedName.contains(it) }
            } ?: boards
            if (labelIds.isEmpty()) {
                return@map sortBoards(boardOrder, result)
            }
            val idsMap = labelIds.groupBy { it }
            val filteredBoards = result.filter { board ->
                board.labels.any { idsMap[it.labelId] != null }
            }
            sortBoards(boardOrder, filteredBoards)
        }
    }

    private fun sortBoards(
        boardOrder: SortingOption.Common,
        boards: List<BoardWithLabelsAndTasks>,
    ): List<BoardWithLabelsAndTasks> = when(boardOrder) {
        SortingOption.Common.NameAZ -> boards.sortedBy { it.board.normalizedName }
        SortingOption.Common.NameZA -> boards.sortedByDescending { it.board.normalizedName }
        SortingOption.Common.Newest -> boards.sortedByDescending { it.board.createdAt }
        SortingOption.Common.Oldest -> boards.sortedBy { it.board.createdAt }
    }
}