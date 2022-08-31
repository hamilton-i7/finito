package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardLabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(vararg labels: BoardLabelCrossRef)

    @Query("SELECT * FROM labeled_boards")
    fun findAll(): Flow<List<BoardLabelCrossRef>>

    @Delete
    suspend fun remove(vararg labels: BoardLabelCrossRef)
}