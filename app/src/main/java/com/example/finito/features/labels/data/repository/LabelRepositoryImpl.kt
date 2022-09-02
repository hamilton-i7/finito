package com.example.finito.features.labels.data.repository

import com.example.finito.features.labels.data.dao.LabelDao
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow

class LabelRepositoryImpl(
    private val dao: LabelDao
) : LabelRepository {
    override suspend fun create(label: Label) {
        return dao.create(label)
    }

    override fun findSimpleLabels(): Flow<List<SimpleLabel>> {
        return dao.findSimpleLabels()
    }

    override suspend fun update(label: Label): Int {
        return dao.update(label)
    }

    override suspend fun remove(label: Label): Int {
        return dao.remove(label)
    }
}