package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.tasks.domain.repository.TaskRepository

class FindUpcomingTasks(
    private val repository: TaskRepository
) {
    operator fun invoke() {}
}