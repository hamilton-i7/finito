package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.data.dao.BoardLabelDao
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.repository.BoardLabelRepository

class BoardLabelRepositoryImpl(
    private val dao: BoardLabelDao
) : BoardLabelRepository {
    override suspend fun create(vararg labels: BoardLabelCrossRef) {
        return dao.create(*labels)
    }

    override suspend fun findAllByBoardId(boardId: Int): List<BoardLabelCrossRef> {
        return dao.findAllByBoardId(boardId)
    }

    override suspend fun remove(vararg labels: BoardLabelCrossRef): Int {
        return dao.remove(*labels)
    }
}