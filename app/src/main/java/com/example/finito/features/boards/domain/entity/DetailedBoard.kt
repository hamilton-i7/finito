package com.example.finito.features.boards.domain.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.finito.features.tasks.domain.entity.DetailedTask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks

data class DetailedBoard(
    @Embedded val board: Board,
    @Relation(
        parentColumn = "board_id",
        entityColumn = "board_id",
        entity = Task::class
    )
    val tasks: List<DetailedTask> = emptyList()
)
