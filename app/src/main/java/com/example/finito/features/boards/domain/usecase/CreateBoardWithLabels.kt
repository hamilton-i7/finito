package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.repository.BoardRepository

class CreateBoardWithLabels(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(board: Board, labels: List<BoardLabelCrossRef>) {
        if (isValid(board, labels)) {
            return repository.create(board, labels)
        }
    }

    @Throws(ResourceException::class, InvalidIdException::class)
    private fun isValid(board: Board, labels: List<BoardLabelCrossRef>): Boolean {
        if (board.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (board.archived && board.deleted) {
            throw ResourceException.InvalidException(
                message = "Board must be either archived or deleted. Not both"
            )
        }
        return labels.all {
            isValidId(it.boardId) && isValidId(it.labelId)
        }
    }
}