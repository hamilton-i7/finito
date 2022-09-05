package com.example.finito.features.subtasks.domain.usecase

data class SubtaskUseCases(
    val findOneSubtask: FindOneSubtask,
    val updateSubtask: UpdateSubtask,
    val deleteSubtask: DeleteSubtask
)