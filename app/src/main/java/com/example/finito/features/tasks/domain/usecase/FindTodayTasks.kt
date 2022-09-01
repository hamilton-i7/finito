package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.labels.domain.util.TaskOrder
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
                    compareBy({ it.task.priority?.ordinal ?: -1 }, { it.task.date })
                )
                TaskOrder.LEAST_URGENT -> tasks.sortedWith(
                    compareBy(
                        {
                            if (it.task.priority == null) 1
                            else -it.task.priority.ordinal
                        }, { it.task.date }
                    )
                )
                null -> tasks.sortedByDescending { it.task.date }
            }
        }
    }
}