package com.example.finito.features.labels.domain.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabels

data class LabelWithBoards(
    @Embedded val label: Label,
    @Relation(
        parentColumn = "label_id",
        entityColumn = "board_id",
        associateBy = Junction(BoardLabelCrossRef::class),
        entity = Board::class
    )
    val boards: List<BoardWithLabels> = emptyList()
)
