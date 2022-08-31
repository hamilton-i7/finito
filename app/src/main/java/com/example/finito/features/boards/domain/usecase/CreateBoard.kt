package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class CreateBoard(
    private val repository: BoardRepository
) {
    @Throws(ResourceException::class)
    suspend operator fun invoke(board: Board) {
        if (board.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (board.archived && board.deleted) {
            throw ResourceException.InvalidException(
                message = "Board must be either archived or deleted. Not both"
            )
        }
        return repository.create(board)
    }
}