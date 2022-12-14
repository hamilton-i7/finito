package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class UpdateLabel(
    private val repository: LabelRepository
) {

    suspend operator fun invoke(label: Label): Result<Unit, String> {
        if (!isValidId(label.labelId)) {
            return Result.Error(ErrorMessages.INVALID_ID)
        }
        if (label.name.isBlank()) {
            return Result.Error(ErrorMessages.EMPTY_NAME)
        }
        repository.findOne(label.labelId) ?: return Result.Error(ErrorMessages.NOT_FOUND)

        return Result.Success(
            data = repository.update(label.copy(
                name = label.name.trim(),
                normalizedName = label.name.trim().normalize())
            )
        )
    }
}