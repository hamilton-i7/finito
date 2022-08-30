package com.example.finito.features.boards.domain.repository

import com.example.finito.features.boards.domain.entity.BoardWithLabels
import kotlinx.coroutines.flow.Flow

interface BoardRepository {

    fun getBoards(): Flow<List<BoardWithLabels>>
}