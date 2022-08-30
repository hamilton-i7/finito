package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    @Transaction
    @Query("SELECT * FROM boards")
    fun getBoards(): Flow<List<BoardWithLabels>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBoard(board: Board)
}