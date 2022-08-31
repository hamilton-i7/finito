package com.example.finito.features.boards.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import java.time.LocalDateTime

data class DetailedBoard(
    @ColumnInfo(name = "board_id") val boardId: Int = 0,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @Relation(
        parentColumn = "board_id",
        entityColumn = "board_id",
        entity = Task::class
    )
    val tasks: List<TaskWithSubtasks> = emptyList()
)
