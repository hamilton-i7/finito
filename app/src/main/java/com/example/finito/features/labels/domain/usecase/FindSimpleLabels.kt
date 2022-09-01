package com.example.finito.features.labels.domain.usecase

import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindSimpleLabels(
    private val repository: LabelRepository
) {
    operator fun invoke(): Flow<List<SimpleLabel>> {
        return repository.findSimpleLabels().map { labels ->
            labels.sortedBy { it.normalizedName }
        }
    }
}