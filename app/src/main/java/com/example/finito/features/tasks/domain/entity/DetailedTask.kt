package com.example.finito.features.tasks.domain.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.finito.features.subtasks.domain.entity.Subtask

data class DetailedTask(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "task_id",
        entityColumn = "task_id",
    )
    val subtasks: List<Subtask> = emptyList()
)
