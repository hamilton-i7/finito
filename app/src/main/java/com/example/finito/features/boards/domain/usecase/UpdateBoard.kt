package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class UpdateBoard(
    private val repository: BoardRepository
) {
    @Throws(
        ResourceException.InvalidIdException::class,
        ResourceException.EmptyException::class,
        ResourceException.InvalidException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(board: Board): Int {
        if (!isValidId(board.boardId)) {
            throw ResourceException.InvalidIdException
        }
        if (board.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (board.archived && board.deleted) {
            throw ResourceException.InvalidException(
                message = "Board must be either archived or deleted. Not both"
            )
        }
        return repository.update(board).let {
            if (it == 0) throw ResourceException.NotFoundException
            else it
        }
    }
}