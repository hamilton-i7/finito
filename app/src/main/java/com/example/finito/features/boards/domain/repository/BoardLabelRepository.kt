package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import kotlinx.coroutines.flow.Flow

interface BoardLabelRepository {

    suspend fun create(vararg labels: BoardLabelCrossRef)

    fun findAll(): Flow<List<BoardLabelCrossRef>>

    suspend fun remove(vararg labels: BoardLabelCrossRef)
}