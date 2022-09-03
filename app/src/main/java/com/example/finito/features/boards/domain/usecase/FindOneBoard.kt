package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.repository.BoardRepository

class FindOneBoard(
    private val repository: BoardRepository
) {
    @Throws(ResourceException.NegativeIdException::class)
    suspend operator fun invoke(id: Int): DetailedBoard {
        if (!isValidId(id)) {
            throw ResourceException.NegativeIdException
        }
        return repository.findOne(id)?.let { board ->
            board.copy(
                tasks = board.tasks.sortedBy { it.task.position }
            )
        } ?: throw ResourceException.NotFoundException
    }
}