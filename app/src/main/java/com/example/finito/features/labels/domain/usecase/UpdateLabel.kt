package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class UpdateLabel(
    private val repository: LabelRepository
) {
    @Throws(ResourceException::class)
    suspend operator fun invoke(label: Label) {
        if (!isValidId(label.labelId)) {
            throw ResourceException.NegativeIdException
        }
        if (label.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        repository.findOne(label.labelId) ?: throw ResourceException.NotFoundException

        return repository.update(label.copy(normalizedName = label.name.normalize()))
    }
}