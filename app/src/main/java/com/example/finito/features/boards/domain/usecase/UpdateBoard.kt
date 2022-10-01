package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.repository.BoardLabelRepository
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.first

class UpdateBoard(
    private val boardRepository: BoardRepository,
    private val boardLabelRepository: BoardLabelRepository
) {

    suspend operator fun invoke(
        boardWithLabelsAndTasks: BoardWithLabelsAndTasks
    ): Result<Unit, String> {
        val (board, labels) = boardWithLabelsAndTasks

        if (!isValidId(board.boardId)) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        if (board.name.isBlank()) {
            return Result.Error(message = ErrorMessages.EMPTY_NAME)
        }

        boardRepository.findOne(board.boardId) ?: return Result.Error(
            message = ErrorMessages.NOT_FOUND
        )
        val formattedBoard = board.copy(
            name = board.name.trim(),
            normalizedName = board.name.trim().normalize()
        )

        return Result.Success(
            data = boardRepository.update(formattedBoard).also {
                // React to board position changes
                arrangeBoards(formattedBoard)
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
        )
    }

    private suspend fun arrangeBoards(board: Board) {
        val boards = boardRepository.findActiveBoards().first().filter {
            it.board.boardId != board.boardId
        }.map { it.board }
        with(boards.toMutableList()) {
            board.position?.let { position ->
                if (position == lastIndex) {
                    add(board)
                } else {
                    add(position, board)
                }
            }
            mapIndexed { index, board -> board.copy(position = index) }.let {
                boardRepository.update(*it.toTypedArray())
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
}