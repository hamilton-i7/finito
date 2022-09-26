package com.example.finito.features.tasks.domain.usecase

data class TaskUseCases(
    val createTask: CreateTask,
    val findTodayTasks: FindTodayTasks,
    val findTomorrowTasks: FindTomorrowTasks,
    val findUrgentTasks: FindUrgentTasks,
    val findOneTask: FindOneTask,
    val updateTask: UpdateTask,
    val deleteTask: DeleteTask,
    val arrangeBoardTasks: ArrangeBoardTasks,
    val arrangeTodayTasks: ArrangeTodayTasks,
    val arrangeTomorrowTasks: ArrangeTomorrowTasks,
    val arrangeUrgentTasks: ArrangeUrgentTasks,
    val toggleTaskCompleted: ToggleTaskCompleted
)