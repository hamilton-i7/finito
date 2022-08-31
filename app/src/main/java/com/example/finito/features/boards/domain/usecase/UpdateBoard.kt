package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class UpdateBoard(
    private val repository: BoardRepository
) {
    @Throws(ResourceException::class, InvalidIdException::class)
    suspend operator fun invoke(board: Board) {
        if (!isValidId(board.boardId)) {
            throw InvalidIdException
        }
        if (board.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (board.archived && board.deleted) {
            throw ResourceException.InvalidException(
                message = "Board must be either archived or deleted. Not both"
            )
        }
        return repository.update(board)
    }
}