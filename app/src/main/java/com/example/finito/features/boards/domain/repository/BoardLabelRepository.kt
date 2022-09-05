package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef

interface BoardLabelRepository {

    suspend fun create(vararg refs: BoardLabelCrossRef)

    suspend fun findAll(): List<BoardLabelCrossRef>

    suspend fun findAllByBoardId(boardId: Int): List<BoardLabelCrossRef>

    suspend fun remove(vararg refs: BoardLabelCrossRef): Int
}