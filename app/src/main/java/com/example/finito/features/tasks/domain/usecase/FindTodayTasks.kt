package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.tasks.domain.util.TaskOrder
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FindTodayTasks(
    private val repository: TaskRepository
) {
    operator fun invoke(
        taskOrder: TaskOrder? = null
    ): Flow<List<TaskWithSubtasks>> {
        return repository.findTodayTasks().map { tasks ->
            when (taskOrder) {
                TaskOrder.MOST_URGENT -> tasks.sortedWith(
                    compareByDescending<TaskWithSubtasks> {
                        it.task.priority?.level
                    }.thenByDescending { it.task.time }
                )
                TaskOrder.LEAST_URGENT -> tasks.sortedWith(
                    compareBy<TaskWithSubtasks> {
                        it.task.priority?.level
                    }.thenByDescending { it.task.time }
                )
                null -> tasks.sortedWith(
                    compareByDescending { it.task.time }
                )
            }
        }
    }
}