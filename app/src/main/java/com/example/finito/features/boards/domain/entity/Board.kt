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
    @ColumnInfo(defaultValue = "'ACTIVE'") val state: BoardState = BoardState.ACTIVE,
    @ColumnInfo(name = "removed_at", defaultValue = "NULL") val removedAt: LocalDateTime? = null,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        private val timestamps = listOf(
            LocalDateTime.now(),
            LocalDateTime.now().plusYears(2),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2),
            LocalDateTime.now().minusMonths(1),
            LocalDateTime.now().plusWeeks(2),
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().plusMinutes(2),
        )

        val dummyBoards = ('A'..'Z').mapIndexed { index, c ->
            Board(
                boardId = index + 1,
                name = "Board $c",
                state = if (index % 2 == 0) BoardState.DELETED
                else if (index % 7 ==0) BoardState.ARCHIVED
                else BoardState.ACTIVE,
                createdAt = LocalDateTime.now().plusMinutes(index.toLong()),
                removedAt = if (index % 2 == 0) timestamps.random() else null,
            )
        }.shuffled()
    }
}

