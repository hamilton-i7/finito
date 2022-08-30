package com.example.finito.features.boards.domain.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.finito.features.labels.domain.entity.Label

@Entity(tableName = "labeled_boards", foreignKeys = [
    ForeignKey(
        entity = Board::class,
        parentColumns = ["boardId"],
        childColumns = ["boardId"],
        onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
        entity = Label::class,
        parentColumns = ["labelId"],
        childColumns = ["labelId"],
        onDelete = ForeignKey.CASCADE
    ),
])
data class BoardLabelCrossRef(
    @PrimaryKey val id: Int,
    val boardId: Int,
    val labelId: Int
)
