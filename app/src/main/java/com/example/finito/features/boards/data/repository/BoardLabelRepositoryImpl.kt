package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.data.dao.BoardLabelDao
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.repository.BoardLabelRepository
import kotlinx.coroutines.flow.Flow

class BoardLabelRepositoryImpl(
    private val dao: BoardLabelDao
) : BoardLabelRepository {
    override suspend fun create(vararg labels: BoardLabelCrossRef) {
        return dao.create(*labels)
    }

    override fun findAll(): Flow<List<BoardLabelCrossRef>> {
        return dao.findAll()
    }

    override suspend fun remove(vararg labels: BoardLabelCrossRef) {
        return dao.remove(*labels)
    }
}