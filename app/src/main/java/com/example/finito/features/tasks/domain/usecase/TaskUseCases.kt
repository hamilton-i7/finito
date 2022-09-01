package com.example.finito.features.tasks.domain.usecase

data class TaskUseCases(
    val createTask: CreateTask,
    val findTodayTasks: FindTodayTasks,
    val findUpcomingTasks: FindUpcomingTasks
)