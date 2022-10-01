package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.repository.BoardLabelRepository
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.first

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
        val formattedBoard = board.copy(name = board.name.trim(), position = 0)
        return boardRepository.create(formattedBoard).toInt().also { boardId ->
            arrangeBoards(formattedBoard.copy(boardId = boardId))

            if (labels.isEmpty()) return@also
            val boardLabelCrossRefs = labels.map { label ->
                BoardLabelCrossRef(boardId = boardId, labelId = label.labelId)
            }.toTypedArray()
            boardLabelRepository.create(*boardLabelCrossRefs)
        }
    }

    private suspend fun arrangeBoards(board: Board) {
        val boards = boardRepository.findActiveBoards().first().filter {
            it.board.boardId != board.boardId
        }.map { it.board }
        with(boards.toMutableList()) {
            add(index = 0, board)
            mapIndexed { index, board -> board.copy(position = index) }.let {
                boardRepository.update(*it.toTypedArray())
            }
        }
    }
}