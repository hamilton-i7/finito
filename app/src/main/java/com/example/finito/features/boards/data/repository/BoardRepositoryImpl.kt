package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.data.dao.BoardDao
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

class BoardRepositoryImpl(
    private val dao: BoardDao
) : BoardRepository {
    override fun getBoards(): Flow<List<BoardWithLabels>> {
        return dao.getBoards()
    }
}