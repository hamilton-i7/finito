package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class CreateBoard(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(board: Board) {
        if (isValidBoard(board)) {
            return repository.create(board)
        }
    }

    @Throws(ResourceException::class)
    private fun isValidBoard(board: Board): Boolean {
        if (board.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        return true
    }
}