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

    override suspend fun findAll(): List<Label> {
        return dao.findAll()
    }

    override fun findSimpleLabels(): Flow<List<SimpleLabel>> {
        return dao.findSimpleLabels()
    }

    override suspend fun findOne(id: Int): Label? {
        return dao.findOne(id)
    }

    override suspend fun update(label: Label) {
        return dao.update(label)
    }

    override suspend fun remove(label: Label) {
        return dao.remove(label)
    }
}