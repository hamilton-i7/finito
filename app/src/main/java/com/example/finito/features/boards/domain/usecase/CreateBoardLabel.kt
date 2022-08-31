package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.repository.BoardRepository

class CreateBoardLabel(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(vararg labels: BoardLabelCrossRef) {
        if (isValid(*labels)) {
            return repository.create(*labels)
        }
    }

    @Throws(InvalidIdException::class)
    private fun isValid(vararg labels: BoardLabelCrossRef): Boolean {
        return labels.all {
            isValidId(it.boardId) && isValidId(it.labelId)
        }
    }
}