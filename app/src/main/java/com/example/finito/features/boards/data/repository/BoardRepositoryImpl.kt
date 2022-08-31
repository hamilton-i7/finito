package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.data.dao.BoardDao
import com.example.finito.features.boards.domain.entity.*
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

class BoardRepositoryImpl(
    private val dao: BoardDao
) : BoardRepository {
    override suspend fun create(board: Board) {
        return dao.create(board)
    }

    override suspend fun create(board: Board, labels: List<BoardLabelCrossRef>) {
        return dao.create(board, labels)
    }

    override suspend fun create(vararg labels: BoardLabelCrossRef) {
        return dao.create(*labels)
    }

    override fun findAll(): Flow<List<BoardWithLabels>> {
        return dao.findAll()
    }

    override fun findSimpleBoards(): Flow<List<SimpleBoard>> {
        return dao.findSimpleBoards()
    }

    override fun findArchivedBoards(): Flow<List<BoardWithLabels>> {
        return dao.findArchivedBoards()
    }

    override fun findDeletedBoards(): Flow<List<BoardWithLabels>> {
        return dao.findDeletedBoards()
    }

    override suspend fun findOne(id: Int): DetailedBoard? {
        return dao.findOne(id)
    }

    override suspend fun update(board: Board) {
        return dao.update(board)
    }

    override suspend fun remove(board: Board) {
        return dao.remove(board)
    }

    override suspend fun remove(vararg labels: BoardLabelCrossRef) {
        return dao.remove(*labels)
    }
}