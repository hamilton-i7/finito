package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import kotlinx.coroutines.flow.Flow

interface BoardRepository {

    suspend fun create(board: Board): Long

    suspend fun findAll(): List<Board>

    fun findActiveBoards(): Flow<List<BoardWithLabelsAndTasks>>

    fun findSimpleBoards(): Flow<List<SimpleBoard>>

    fun findArchivedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    fun findDeletedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    suspend fun findDeletedBoardsAsync(): List<Board>

    suspend fun findOne(id: Int): DetailedBoard?

    suspend fun update(vararg boards: Board)

    suspend fun remove(vararg boards: Board)
}