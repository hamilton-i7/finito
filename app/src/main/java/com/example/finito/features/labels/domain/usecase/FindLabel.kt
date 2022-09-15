package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class FindLabel(
    private val repository: LabelRepository,
) {
    @Throws(
        ResourceException.NegativeIdException::class,
        ResourceException.NotFoundException::class
    )
    suspend operator fun invoke(id: Int): Label {
        if (!isValidId(id)) {
            throw ResourceException.NegativeIdException
        }
        return repository.findOne(id) ?: throw ResourceException.NotFoundException
    }
}