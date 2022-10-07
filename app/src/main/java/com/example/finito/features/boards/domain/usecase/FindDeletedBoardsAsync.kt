package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class FindDeletedBoardsAsync(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(): Result.Success<List<Board>> {
        return Result.Success(data = repository.findDeletedBoardsAsync())
    }
}