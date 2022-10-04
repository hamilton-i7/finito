package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.repository.BoardRepository

class DeleteBoard(
    private val repository: BoardRepository
) {

    suspend operator fun invoke(vararg boards: Board): Result<Unit, String> {
        if (boards.any { !isValidId(it.boardId) }) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }

        with(repository.findAll()) {
            val idsMap = groupBy { it.boardId }
            if (boards.any { idsMap[it.boardId] == null }) return Result.Error(ErrorMessages.NOT_FOUND)
            return Result.Success(data = repository.remove(*boards))
        }
    }
}