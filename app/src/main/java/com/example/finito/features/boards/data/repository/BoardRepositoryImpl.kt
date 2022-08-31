package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.data.dao.BoardDao
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

class BoardRepositoryImpl(
    private val dao: BoardDao
) : BoardRepository {
    override fun findAll(): Flow<List<BoardWithLabels>> {
        return dao.findAll()
    }

    override suspend fun create(board: Board) {
        return dao.create(board)
    }
}