package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef

@Dao
interface BoardLabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(vararg labels: BoardLabelCrossRef)

    @Query("SELECT * FROM labeled_boards WHERE board_id = :boardId")
    suspend fun findAllByBoardId(boardId: Int): List<BoardLabelCrossRef>

    @Delete
    suspend fun remove(vararg labels: BoardLabelCrossRef): Int
}