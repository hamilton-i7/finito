package com.example.finito.features.labels.domain.repository

import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.SimpleLabel
import kotlinx.coroutines.flow.Flow

interface LabelRepository {

    suspend fun create(label: Label)

    fun findSimpleLabels(): Flow<List<SimpleLabel>>

    suspend fun update(label: Label): Int

    suspend fun remove(label: Label): Int
}