package com.example.finito.features.labels.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.isValidId
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.repository.LabelRepository
import kotlin.jvm.Throws

class DeleteLabel(
    private val repository: LabelRepository
) {
    @Throws(InvalidIdException::class)
    suspend operator fun invoke(label: Label) {
        if (!isValidId(label.labelId)) {
            throw InvalidIdException
        }
        return repository.remove(label)
    }
}