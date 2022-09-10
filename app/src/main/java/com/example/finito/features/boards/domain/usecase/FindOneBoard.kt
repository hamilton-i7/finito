package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindOneBoard(
    private val repository: BoardRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.NotFoundException::class
    )
    operator fun invoke(id: Int): Flow<DetailedBoard> {
        if (!isValidId(id)) {
            throw ResourceException.NegativeIdException
        }
        return repository.findOne(id).map { detailedBoard ->
            if (detailedBoard == null) throw ResourceException.NotFoundException
            detailedBoard.copy(
                tasks = detailedBoard.tasks.sortedBy { it.task.boardPosition }
            )
        }
    }
}