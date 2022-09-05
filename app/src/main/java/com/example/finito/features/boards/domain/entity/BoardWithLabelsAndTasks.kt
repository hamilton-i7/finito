package com.example.finito.features.boards.domain.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.tasks.domain.entity.CompletedTask
import com.example.finito.features.tasks.domain.entity.Task

data class BoardWithLabelsAndTasks(
    @Embedded val board: Board,
    @Relation(
        parentColumn = "board_id",
        entityColumn = "label_id",
        associateBy = Junction(BoardLabelCrossRef::class),
        entity = Label::class
    )
    val labels: List<SimpleLabel> = emptyList(),
    @Relation(
        parentColumn = "board_id",
        entityColumn = "board_id",
        entity = Task::class
    )
    val tasks: List<CompletedTask> = emptyList()
)