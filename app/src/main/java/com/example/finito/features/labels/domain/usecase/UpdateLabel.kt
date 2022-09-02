package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.core.util.isValidId
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class UpdateLabel(
    private val repository: LabelRepository
) {
    @Throws(ResourceException::class)
    suspend operator fun invoke(label: Label): Int {
        if (!isValidId(label.labelId)) {
            throw ResourceException.InvalidIdException
        }
        if (label.name.isBlank()) {
            throw ResourceException.EmptyException
        }
        return repository.update(label).let {
            if (it == 0) throw ResourceException.NotFoundException
            else it
        }
    }
}