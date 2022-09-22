package com.example.finito.features.tasks.domain.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.finito.core.domain.util.takeRandom
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
                task = Task.dummyTasks[index],
                subtasks = Subtask.dummySubtasks.takeRandom()
            )
        }
    }
}

fun List<TaskWithSubtasks>.filterUncompleted(): List<TaskWithSubtasks> {
    return filter { !it.task.completed }
}

fun List<TaskWithSubtasks>.filterCompleted(): List<TaskWithSubtasks> {
    return filter { it.task.completed }
}
