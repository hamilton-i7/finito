package com.example.finito.features.subtasks.domain.entity

import androidx.room.*
import com.example.finito.core.domain.util.normalize
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
        Index(
            name = "index_subtasks_task_id",
            value = ["task_id"]
        )
    ]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "subtask_id") val subtaskId: Int = 0,
    @ColumnInfo(name = "task_id") val taskId: Int = 0,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    @ColumnInfo(defaultValue = "NULL") val description: String? = null,
    @ColumnInfo(defaultValue = "0") val completed: Boolean = false,
    @ColumnInfo(defaultValue = "NULL") val position: Int? = null,
    @ColumnInfo(name = "completed_at", defaultValue = "NULL")
    val completedAt: LocalDateTime? = null,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        val dummySubtasks = ('A'..'Z').mapIndexed { index, c ->
            Subtask(
                subtaskId = index + 1,
                name = "Subtask $c",
                description = if (index % 2 == 0) "Lorem ipsum dolor sit amet, consectetur adipiscing elit." else null,
                taskId = Task.dummyTasks.random().taskId,
                completed = index % 5 == 0,
                completedAt = if (index % 5 == 0) LocalDateTime.now().plusMinutes((0..1_500).random().toLong()) else null,
            )
        }.shuffled()
    }
}
