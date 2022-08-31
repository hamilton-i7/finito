package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindSimpleBoards(
    private val repository: BoardRepository
) {
    operator fun invoke(): Flow<List<SimpleBoard>> {
        return repository.findSimpleBoards().map { boards ->
            boards.sortedBy { it.normalizedName }
        }
    }
}