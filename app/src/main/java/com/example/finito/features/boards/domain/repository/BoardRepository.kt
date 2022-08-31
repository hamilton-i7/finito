package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.*
import kotlinx.coroutines.flow.Flow

interface BoardRepository {

    suspend fun create(board: Board)

    suspend fun create(board: Board, labels: List<BoardLabelCrossRef>)

    suspend fun create(vararg labels: BoardLabelCrossRef)

    fun findAll(): Flow<List<BoardWithLabels>>

    fun findSimpleBoards(): Flow<List<SimpleBoard>>

    fun findArchivedBoards(): Flow<List<BoardWithLabels>>

    fun findDeletedBoards(): Flow<List<BoardWithLabels>>

    suspend fun findOne(id: Int): DetailedBoard?

    suspend fun update(board: Board)

    suspend fun remove(board: Board)

    suspend fun remove(vararg labels: BoardLabelCrossRef)
}