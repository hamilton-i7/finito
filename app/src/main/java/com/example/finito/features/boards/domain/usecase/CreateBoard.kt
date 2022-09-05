package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.repository.BoardLabelRepository
import com.example.finito.features.boards.domain.repository.BoardRepository

class CreateBoard(
    private val boardRepository: BoardRepository,
    private val boardLabelRepository: BoardLabelRepository
) {
    @Throws(ResourceException::class)
    suspend operator fun invoke(boardWithLabelsAndTasks: BoardWithLabelsAndTasks): Int {
        val (board, labels) = boardWithLabelsAndTasks

        if (board.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (board.archived && board.deleted) {
            throw ResourceException.InvalidStateException(
                message = "Board must be either archived or deleted. Not both"
            )
        }
        return boardRepository.create(board).toInt().also { boardId ->
            if (labels.isEmpty()) return@also
            val boardLabelCrossRefs = labels.map { label ->
                BoardLabelCrossRef(boardId = boardId, labelId = label.labelId)
            }.toTypedArray()
            boardLabelRepository.create(*boardLabelCrossRefs)
        }
    }
}