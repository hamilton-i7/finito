package com.example.finito.features.labels.domain.usecase

data class LabelUseCases(
    val createLabel: CreateLabel,
    val findSimpleLabels: FindSimpleLabels,
    val findLabel: FindLabel,
    val updateLabel: UpdateLabel,
    val deleteLabel: DeleteLabel
)