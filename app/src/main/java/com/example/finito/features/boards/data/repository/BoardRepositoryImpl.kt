package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.data.dao.BoardDao
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

class BoardRepositoryImpl(
    private val dao: BoardDao
) : BoardRepository {
    override suspend fun create(board: Board): Long {
        return dao.create(board)
    }

    override suspend fun findAll(): List<Board> {
        return dao.findAll()
    }

    override fun findActiveBoards(): Flow<List<BoardWithLabelsAndTasks>> {
        return dao.findActiveBoards()
    }

    override fun findSimpleBoards(): Flow<List<SimpleBoard>> {
        return dao.findSimpleBoards()
    }

    override fun findArchivedBoards(): Flow<List<BoardWithLabelsAndTasks>> {
        return dao.findArchivedBoards()
    }

    override fun findDeletedBoards(): Flow<List<BoardWithLabelsAndTasks>> {
        return dao.findDeletedBoards()
    }

    override suspend fun findOne(id: Int): DetailedBoard? {
        return dao.findOne(id)
    }

    override suspend fun update(vararg board: Board) {
        return dao.update(*board)
    }

    override suspend fun remove(vararg boards: Board) {
        return dao.remove(*boards)
    }
}