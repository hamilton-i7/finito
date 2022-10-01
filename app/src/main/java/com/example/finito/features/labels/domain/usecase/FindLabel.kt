package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.isValidId
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository

class FindLabel(
    private val repository: LabelRepository,
) {

    suspend operator fun invoke(id: Int): Result<Label, String> {
        if (!isValidId(id)) {
            return Result.Error(message = ErrorMessages.INVALID_ID)
        }
        return repository.findOne(id)?.let {
            Result.Success(data = it)
        } ?: return Result.Error(message = ErrorMessages.NOT_FOUND)
    }
}