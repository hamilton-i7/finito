package com.example.finito.features.labels.data.repository

import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.LabelWithBoards
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeLabelRepository : LabelRepository {

    private val labels = mutableListOf<LabelWithBoards>()

    override suspend fun create(label: Label) {
        labels.add(LabelWithBoards(
            label = label,
            boards = emptyList()
        ))
    }

    override fun findSimpleLabels(): Flow<List<SimpleLabel>> {
        return flow {
            emit(labels.map {
                SimpleLabel(
                    labelId = it.label.labelId,
                    name = it.label.name,
                )
            })
        }
    }

    override suspend fun findOne(id: Int): LabelWithBoards? {
        return labels.find { it.label.labelId == id }
    }

    override suspend fun update(label: Label) {
        labels.find { it.label.labelId == label.labelId } ?: return
        labels.set(
            index = labels.indexOfFirst { it.label.labelId == label.labelId },
            element = LabelWithBoards(label)
        )
    }

    override suspend fun remove(label: Label) {
        val labelToDeleteIndex = labels.indexOfFirst { it.label.labelId == label.labelId }
        if (labelToDeleteIndex == -1) return

        labels.removeAt(labelToDeleteIndex)
    }
}