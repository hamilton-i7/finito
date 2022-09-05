package com.example.finito.features.labels.data.repository

import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeLabelRepository : LabelRepository {
    private val labels = mutableListOf<Label>()
    private var labelId = 1

    override suspend fun create(label: Label) {
        labels.add(label.copy(labelId = labelId))
        labelId++
    }

    override suspend fun findAll(): List<Label> {
        return labels.toList()
    }

    override fun findSimpleLabels(): Flow<List<SimpleLabel>> {
        return flow {
            emit(labels.map {
                SimpleLabel(
                    labelId = it.labelId,
                    name = it.name,
                )
            })
        }
    }

    override suspend fun findOne(id: Int): Label? {
        return labels.find { it.labelId == id }
    }

    override suspend fun update(label: Label) {
        labels.find { it.labelId == label.labelId }
        labels.set(
            index = labels.indexOfFirst { it.labelId == label.labelId },
            element = label
        )
    }

    override suspend fun remove(label: Label) {
        val labelToDeleteIndex = labels.indexOfFirst { it.labelId == label.labelId }
        labels.removeAt(labelToDeleteIndex)
    }
}