package com.example.finito.features.subtasks.domain.entity

import androidx.room.ColumnInfo

data class CompletedSubtask(
    @ColumnInfo(name = "subtask_id") val subtaskId: Int,
    val completed: Boolean = false,
)
