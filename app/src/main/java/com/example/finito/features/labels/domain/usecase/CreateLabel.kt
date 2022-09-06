package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository
import kotlin.jvm.Throws

class CreateLabel(
    private val repository: LabelRepository
) {

    @Throws(ResourceException::class)
    suspend operator fun invoke(label: Label) {
        if (label.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        return repository.create(label)
    }
}