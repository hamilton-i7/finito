package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.*
import kotlinx.coroutines.flow.Flow

interface BoardRepository {

    suspend fun create(board: Board): Long

    suspend fun findAll(): List<Board>

    fun findActiveBoards(): Flow<List<BoardWithLabelsAndTasks>>

    fun findSimpleBoards(): Flow<List<SimpleBoard>>

    fun findArchivedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    fun findDeletedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    fun findOne(id: Int): Flow<DetailedBoard?>

    suspend fun update(board: Board)

    suspend fun remove(vararg boards: Board)
}