package com.example.finito.features.boards.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    @Transaction
    @Query("SELECT * FROM boards")
    fun getBoards(): Flow<List<BoardWithLabels>>
}