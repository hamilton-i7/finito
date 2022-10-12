package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class CreateLabel(
    private val repository: LabelRepository
) {

    suspend operator fun invoke(label: Label): Result<Unit, String> {
        if (label.name.isBlank()) {
            return Result.Error(message = ErrorMessages.EMPTY_NAME)
        }
        return Result.Success(
            data = repository.create(label.copy(name = label.name.trim()))
        )
    }
}