package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.repository.BoardLabelRepository
import com.example.finito.features.boards.domain.repository.BoardRepository

class UpdateBoard(
    private val boardRepository: BoardRepository,
    private val boardLabelRepository: BoardLabelRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.EmptyException::class,
        ResourceException.InvalidStateException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(boardWithLabels: BoardWithLabels): Int {
        val (board, labels) = boardWithLabels

        if (!isValidId(board.boardId)) {
            throw ResourceException.NegativeIdException
        }
        if (board.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        if (board.archived && board.deleted) {
            throw ResourceException.InvalidStateException(
                message = "Board must be either archived or deleted. Not both"
            )
        }
        return boardRepository.update(board).let {
            if (it == 0) throw ResourceException.NotFoundException
            else it
        }.also {
            with(boardLabelRepository.findAllByBoardId(board.boardId)) {
                if (isEmpty()) return@with
                val newRefs = labels.map {
                    BoardLabelCrossRef(boardId = board.boardId, labelId = it.labelId)
                }
                // Delete refs not found in the old refs list
                deleteRefs(oldRefs= this, newRefs = newRefs, boardLabelRepository)
                // Create the new refs
                createRefs(refs = newRefs, boardLabelRepository)
            }
        }
    }

    private suspend fun createRefs(
        refs: List<BoardLabelCrossRef>,
        boardLabelRepository: BoardLabelRepository,
    ) {
        boardLabelRepository.create(*refs.toTypedArray())
    }

    private suspend fun deleteRefs(
        oldRefs: List<BoardLabelCrossRef>,
        newRefs: List<BoardLabelCrossRef>,
        boardLabelRepository: BoardLabelRepository,
    ) {
        val refs = newRefs.groupBy { it }
        oldRefs.filter { refs[it] == null }.let {
            val deletedAmount = boardLabelRepository.remove(*it.toTypedArray())
            if (deletedAmount != it.size) throw ResourceException.NotFoundException
        }
    }
}