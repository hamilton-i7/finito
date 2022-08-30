package com.example.finito.features.boards.domain.entity

import androidx.room.*
import com.example.finito.features.labels.domain.entity.Label
import java.time.LocalDateTime

@Entity(tableName = "boards")
data class Board(
    @PrimaryKey val boardId: Int,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class BoardWithLabels(
    @Embedded val board: Board,
    @Relation(
        parentColumn = "boardId",
        entityColumn = "labelId",
        associateBy = Junction(BoardLabelCrossRef::class)
    )
    val labels: List<Label>
)