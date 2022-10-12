package com.example.finito.features.tasks.domain.entity

import android.os.Parcelable
import androidx.room.*
import com.example.finito.core.domain.util.normalize
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.tasks.domain.util.Priority
import kotlinx.parcelize.Parcelize
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
@Parcelize
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
    @ColumnInfo(defaultValue = "NULL") val priority: Priority? = null,
    @ColumnInfo(name = "board_position", defaultValue = "NULL") val boardPosition: Int? = null,
    @ColumnInfo(name = "completed_at", defaultValue = "NULL")
    val completedAt: LocalDateTime? = null,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) : Parcelable {
    companion object {
        val dummyTasks = ('A'..'Z').mapIndexed { index, c ->
            Task(
                taskId = index + 1,
                name = "Task $c",
                description = if (index % 2 == 0) "Lorem ipsum dolor sit amet, consectetur adipiscing elit." else null,
                boardId = Board.dummyBoards.random().boardId,
                completed = index % 5 == 0,
                date = if (index % 3 == 0) LocalDate.now().plusDays((0..100).random().toLong()) else null,
                time = if (index % 6 == 0) LocalTime.now().plusHours((0..200).random().toLong()) else null,
                completedAt = if (index % 5 == 0) LocalDateTime.now().plusMinutes((0..1_500).random().toLong()) else null,
                priority = if (index % 9 == 0) enumValues<Priority>().random() else null,
            )
        }.shuffled()
    }
}

