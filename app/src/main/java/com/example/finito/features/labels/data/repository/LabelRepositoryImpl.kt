package com.example.finito.features.labels.data.repository

import com.example.finito.features.labels.data.dao.LabelDao
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class LabelRepositoryImpl(
    private val dao: LabelDao
) : LabelRepository {
    override suspend fun create(label: Label) {
        return dao.create(label)
    }
}