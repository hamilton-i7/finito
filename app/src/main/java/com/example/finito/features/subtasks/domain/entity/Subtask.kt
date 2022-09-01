package com.example.finito.features.subtasks.domain.entity

import androidx.room.*
import com.example.finito.core.Priority
import com.example.finito.core.domain.Reminder
import com.example.finito.core.util.normalize
import com.example.finito.features.tasks.domain.entity.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["task_id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["task_id"])
    ]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "subtask_id") val subtaskId: Int = 0,
    @ColumnInfo(name = "task_id") val taskId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    val description: String? = null,
    val completed: Boolean = false,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val reminder: Reminder? = null,
    val priority: Priority? = null,
    val position: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
)
