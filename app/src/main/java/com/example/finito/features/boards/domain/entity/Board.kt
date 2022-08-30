package com.example.finito.features.boards.domain.entity

import androidx.room.*
import com.example.finito.core.util.normalize
import com.example.finito.features.labels.domain.entity.Label
import java.time.LocalDateTime

@Entity(tableName = "boards")
data class Board(
    @PrimaryKey
    @ColumnInfo(name = "board_id") val boardId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class BoardWithLabels(
    @Embedded val board: Board,
    @Relation(
        parentColumn = "board_id",
        entityColumn = "label_id",
        associateBy = Junction(BoardLabelCrossRef::class)
    )
    val labels: List<Label>
)