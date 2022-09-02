package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class DeleteBoard(
    private val repository: BoardRepository
) {
    @Throws(
        ResourceException.InvalidIdException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(board: Board): Int {
        if (!isValidId(board.boardId)) {
            throw ResourceException.InvalidIdException
        }
        return repository.remove(board).let {
            if (it == 0) throw ResourceException.NotFoundException
            else it
        }
    }
}