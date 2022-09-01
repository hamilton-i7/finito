package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindDeletedBoards(
    private val repository: BoardRepository
) {
    operator fun invoke(): Flow<List<BoardWithLabels>> {
        return repository.findDeletedBoards().map { boards ->
            boards.sortedBy { it.board.normalizedName }
        }
    }
}