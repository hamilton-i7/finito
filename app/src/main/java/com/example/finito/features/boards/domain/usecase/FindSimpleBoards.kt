package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindSimpleBoards(
    private val repository: BoardRepository
) {
    operator fun invoke(): Result.Success<Flow<List<SimpleBoard>>> {
        return Result.Success(
            data = repository.findSimpleBoards().map { boards ->
                boards.sortedBy { it.normalizedName }
            }
        )
    }
}