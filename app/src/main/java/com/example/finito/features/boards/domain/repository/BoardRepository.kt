package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import kotlinx.coroutines.flow.Flow

interface BoardRepository {

    fun findAll(): Flow<List<BoardWithLabels>>

    suspend fun create(board: Board)
}