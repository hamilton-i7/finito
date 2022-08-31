package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBoardRepository : BoardRepository {

    private val boards = mutableListOf<BoardWithLabels>()

    override fun findAll(): Flow<List<BoardWithLabels>> {
        return flow { emit(boards) }
    }

    override suspend fun create(board: Board) {
        boards.add(BoardWithLabels(
            board = board,
            labels = emptyList()
        ))
    }
}