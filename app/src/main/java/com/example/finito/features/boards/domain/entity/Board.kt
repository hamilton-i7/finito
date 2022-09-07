package com.example.finito.features.boards.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finito.core.domain.util.normalize
import java.time.LocalDateTime

@Entity(tableName = "boards")
data class Board(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "board_id") val boardId: Int = 0,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false,
    @ColumnInfo(defaultValue = "0") val deleted: Boolean = false,
    @ColumnInfo(name = "removed_at", defaultValue = "NULL") val removedAt: LocalDateTime? = null,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        val dummyBoards = ('A'..'Z').mapIndexed { index, c ->
            Board(
                boardId = index + 1,
                name = "Board $c",
                archived = index % 2 == 0,
                deleted = index % 7 == 0 && index % 2 != 0
            )
        }.shuffled()
    }
}

