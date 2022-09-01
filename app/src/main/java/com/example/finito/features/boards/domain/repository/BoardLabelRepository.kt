package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef

interface BoardLabelRepository {

    suspend fun create(vararg labels: BoardLabelCrossRef)

    suspend fun remove(vararg labels: BoardLabelCrossRef)
}