package com.example.finito.features.boards.domain.entity

import androidx.room.ColumnInfo
import com.example.finito.core.domain.util.normalize

data class SimpleBoard(
    @ColumnInfo(name = "board_id") val boardId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
)
