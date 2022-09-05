package com.example.finito.features.subtasks.domain.entity

import androidx.room.ColumnInfo

data class SimpleSubtask(
    @ColumnInfo(name = "subtask_id") val subtaskId: Int = 0,
    val name: String,
    val position: Int = 0
)
