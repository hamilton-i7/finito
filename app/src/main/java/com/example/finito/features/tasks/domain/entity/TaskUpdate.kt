package com.example.finito.features.tasks.domain.entity

import androidx.room.ColumnInfo
import com.example.finito.core.Priority
import com.example.finito.core.domain.Reminder
import java.time.LocalDate
import java.time.LocalTime

data class TaskUpdate(
    @ColumnInfo(name = "task_id") val taskId: Int = 0,
    @ColumnInfo(name = "board_id") val boardId: Int,
    val name: String,
    val description: String? = null,
    val completed: Boolean = false,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val reminder: Reminder? = null,
    val priority: Priority? = null,
)
