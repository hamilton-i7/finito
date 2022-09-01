package com.example.finito.features.boards.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef

@Dao
interface BoardLabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(vararg labels: BoardLabelCrossRef)

    @Delete
    suspend fun remove(vararg labels: BoardLabelCrossRef)
}