package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindSimpleLabels(
    private val repository: LabelRepository
) {
    operator fun invoke(searchQuery: String? = null): Result.Success<Flow<List<SimpleLabel>>> {
        return Result.Success(
            data = repository.findSimpleLabels().map { labels ->
                val normalizedQuery = searchQuery?.normalize()
                normalizedQuery?.let {
                    labels.filter { label ->
                        label.normalizedName.contains(it)
                    }.sortedBy { it.normalizedName }
                } ?: labels.sortedBy { it.normalizedName }
            }
        )
    }
}