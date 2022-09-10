package com.example.finito.features.tasks.domain.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.finito.features.subtasks.domain.entity.Subtask

data class TaskWithSubtasks(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "task_id",
        entityColumn = "task_id",
    )
    val subtasks: List<Subtask> = emptyList()
) {
    companion object {
        val dummyTasks = ('A'..'Z').mapIndexed { index, _ ->
            TaskWithSubtasks(
                task = Task.dummyTasks.random(),
                subtasks = Subtask.dummySubtasks.take(index)
            )
        }
    }
}
