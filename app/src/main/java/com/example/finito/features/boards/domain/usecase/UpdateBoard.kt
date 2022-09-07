package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
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
    suspend operator fun invoke(boardWithLabelsAndTasks: BoardWithLabelsAndTasks) {
        val (board, labels) = boardWithLabelsAndTasks

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
        val oldBoard = boardRepository.findOne(board.boardId) ?: throw ResourceException.NotFoundException
        val result = setupTrashPosition(oldBoard.board, board)

        return boardRepository.update(result).also {
            with(boardLabelRepository.findAllByBoardId(board.boardId)) {
                val newRefs = labels.map {
                    BoardLabelCrossRef(boardId = board.boardId, labelId = it.labelId)
                }
                if (this == newRefs) return@with
                // Delete refs not found in the old refs list
                deleteRefs(oldRefs= this, newRefs = newRefs)
                // Create the new refs
                if (labels.isNotEmpty()) {
                    createRefs(refs = newRefs)
                }
            }
        }
    }

    private suspend fun createRefs(refs: List<BoardLabelCrossRef>) {
        boardLabelRepository.create(*refs.toTypedArray())
    }

    private suspend fun deleteRefs(
        oldRefs: List<BoardLabelCrossRef>,
        newRefs: List<BoardLabelCrossRef>,
    ) {
        val refs = newRefs.groupBy { it }
        oldRefs.filter { refs[it] == null }.let {
            val deletedAmount = boardLabelRepository.remove(*it.toTypedArray())
            if (deletedAmount != it.size) throw ResourceException.NotFoundException
        }
    }

    private suspend fun setupTrashPosition(oldBoard: Board, newBoard: Board): Board {
        if (oldBoard.deleted == newBoard.deleted) return newBoard
        return if (newBoard.deleted) {
            val boardsInTrashAmount = boardRepository.findAll().filter { it.deleted }.size
            newBoard.copy(trashPosition = boardsInTrashAmount)
        } else newBoard.copy(trashPosition = -1)
    }
}