package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.repository.BoardRepository

class FindOneBoard(
    private val repository: BoardRepository
) {
    @Throws(InvalidIdException::class)
    suspend operator fun invoke(id: Int): DetailedBoard? {
        if (!isValidId(id)) {
            throw InvalidIdException
        }
        return repository.findOne(id)?.let { board ->
            board.copy(
                tasks = board.tasks.sortedBy { it.task.position }
            )
        }
    }
}