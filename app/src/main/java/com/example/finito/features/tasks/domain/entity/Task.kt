package com.example.finito.features.tasks.domain.entity

import androidx.room.*
import com.example.finito.core.domain.Priority
import com.example.finito.core.domain.Reminder
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.boards.domain.entity.Board
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Board::class,
            parentColumns = ["board_id"],
            childColumns = ["board_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["board_id"])
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id") val taskId: Int = 0,
    @ColumnInfo(name = "board_id") val boardId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    val description: String? = null,
    val completed: Boolean = false,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val reminder: Reminder? = null,
    val priority: Priority? = null,
    @ColumnInfo(name = "board_position") val boardPosition: Int = -1,
    @ColumnInfo(name = "today_position") val todayPosition: Int = -1,
    @ColumnInfo(name = "tomorrow_position") val tomorrowPosition: Int = -1,
    @ColumnInfo(name = "urgent_position") val urgentPosition: Int = -1,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
)

fun Task.toTaskUpdate(): TaskUpdate {
    return TaskUpdate(
        taskId, boardId, name, description, completed, date, time, reminder, priority
    )
}