package com.example.finito.features.labels.domain.repository

import com.example.finito.features.labels.domain.entity.Label

interface LabelRepository {

    suspend fun create(label: Label)
}