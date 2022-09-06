package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class DeleteBoard(
    private val repository: BoardRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(board: Board) {
        if (!isValidId(board.boardId)) {
            throw ResourceException.NegativeIdException
        }
        repository.findOne(board.boardId) ?: throw ResourceException.NotFoundException
        return repository.remove(board)
    }
}