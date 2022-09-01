package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FindUrgentTasks(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<Map<LocalDate?, List<TaskWithSubtasks>>> {
        return repository.findUrgentTasks().map { tasks ->
            val sortedTasks = tasks.sortedWith(
                compareByDescending<TaskWithSubtasks> {
                    it.task.date
                }.thenByDescending { it.task.time }
            )
            sortedTasks.groupBy { it.task.date }
        }
    }
}