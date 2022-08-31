package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(board: Board)

    @Transaction
    @Query("SELECT * FROM boards")
    fun findAll(): Flow<List<BoardWithLabels>>
}