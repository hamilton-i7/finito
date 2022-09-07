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
    suspend operator fun invoke(vararg boards: Board) {
        if (boards.any { !isValidId(it.boardId) }) {
            throw ResourceException.NegativeIdException
        }

        with(repository.findAll()) {
            val idsMap = groupBy { it.boardId }
            if (boards.any { idsMap[it.boardId] == null }) throw ResourceException.NotFoundException
            return repository.remove(*boards)
        }
    }
}