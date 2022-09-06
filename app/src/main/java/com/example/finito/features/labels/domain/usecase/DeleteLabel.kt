package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class DeleteLabel(
    private val repository: LabelRepository
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(label: Label) {
        if (!isValidId(label.labelId)) {
            throw ResourceException.NegativeIdException
        }
        repository.findOne(label.labelId) ?: throw ResourceException.NotFoundException

        return repository.remove(label)
    }
}