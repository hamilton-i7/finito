package com.example.finito.features.subtasks.domain.entity

import androidx.room.*
import com.example.finito.core.util.normalize
import com.example.finito.features.tasks.domain.entity.Task
import java.time.LocalDateTime

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
    val position: Int? = null,
    @ColumnInfo(name = "completed_position") val completedPosition: Int? = null,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
)
