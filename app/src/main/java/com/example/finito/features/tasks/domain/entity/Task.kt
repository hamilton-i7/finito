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
        Index(
            name = "index_tasks_board_id",
            value = ["board_id"]
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id") val taskId: Int = 0,
    @ColumnInfo(name = "board_id") val boardId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    @ColumnInfo(defaultValue = "NULL") val description: String? = null,
    @ColumnInfo(defaultValue = "0") val completed: Boolean = false,
    @ColumnInfo(defaultValue = "NULL") val date: LocalDate? = null,
    @ColumnInfo(defaultValue = "NULL") val time: LocalTime? = null,
    @ColumnInfo(defaultValue = "NULL") val reminder: Reminder? = null,
    @ColumnInfo(defaultValue = "NULL") val priority: Priority? = null,
    @ColumnInfo(name = "board_position", defaultValue = "-1") val boardPosition: Int = -1,
    @ColumnInfo(name = "today_position", defaultValue = "-1") val todayPosition: Int = -1,
    @ColumnInfo(name = "tomorrow_position", defaultValue = "-1") val tomorrowPosition: Int = -1,
    @ColumnInfo(name = "urgent_position", defaultValue = "-1") val urgentPosition: Int = -1,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

fun Task.toTaskUpdate(): TaskUpdate {
    return TaskUpdate(
        taskId, boardId, name, description, completed, date, time, reminder, priority
    )
}