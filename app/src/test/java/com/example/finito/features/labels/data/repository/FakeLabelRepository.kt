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

    fun findOne(id: Int): Label? {
        return labels.find { it.labelId == id }
    }

    override suspend fun update(label: Label): Int {
        labels.find { it.labelId == label.labelId } ?: return 0
        labels.set(
            index = labels.indexOfFirst { it.labelId == label.labelId },
            element = label
        )
        return 1
    }

    override suspend fun remove(label: Label): Int {
        val labelToDeleteIndex = labels.indexOfFirst { it.labelId == label.labelId }
        if (labelToDeleteIndex == -1) return 0

        labels.removeAt(labelToDeleteIndex)
        return 1
    }
}