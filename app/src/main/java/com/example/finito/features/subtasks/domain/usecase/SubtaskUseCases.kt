package com.example.finito.features.subtasks.domain.usecase

data class SubtaskUseCases(
    val createManySubtasks: CreateManySubtasks,
    val updateManySubtasks: UpdateManySubtasks,
    val deleteSubtask: DeleteSubtask
)