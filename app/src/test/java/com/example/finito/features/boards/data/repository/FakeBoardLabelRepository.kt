package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.repository.BoardLabelRepository

class FakeBoardLabelRepository : BoardLabelRepository {
    private val boardLabelRefs = mutableListOf<BoardLabelCrossRef>()

    override suspend fun create(vararg refs: BoardLabelCrossRef) {
        boardLabelRefs.addAll(refs)
    }

    override suspend fun findAll(): List<BoardLabelCrossRef> {
        return boardLabelRefs.toList()
    }

    override suspend fun findAllByBoardId(boardId: Int): List<BoardLabelCrossRef> {
        return boardLabelRefs.filter { it.boardId == boardId }
    }

    override suspend fun remove(vararg refs: BoardLabelCrossRef): Int {
        var deleteCount = 0
        refs.forEach {
            boardLabelRefs.remove(it).also { deleted ->
                if (!deleted) return@also
                deleteCount++
            }
        }
        return deleteCount
    }
}