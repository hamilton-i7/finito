package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlin.jvm.Throws

class DeleteBoard(
    private val repository: BoardRepository
) {
    @Throws(InvalidIdException::class)
    suspend operator fun invoke(board: Board) {
        if (!isValidId(board.boardId)) {
            throw InvalidIdException
        }
        return repository.remove(board)
    }
}