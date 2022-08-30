package com.example.finito.features.boards.domain.entity

import androidx.room.*
import com.example.finito.features.labels.domain.entity.Label

@Entity(
    tableName = "labeled_boards", primaryKeys = ["board_id", "label_id"], foreignKeys = [
    ForeignKey(
        entity = Board::class,
        parentColumns = ["board_id"],
        childColumns = ["board_id"],
        onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
        entity = Label::class,
        parentColumns = ["label_id"],
        childColumns = ["label_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["label_id"])
    ]
)
data class BoardLabelCrossRef(
    @ColumnInfo(name = "board_id") val boardId: Int,
    @ColumnInfo(name = "label_id") val labelId: Int
)
