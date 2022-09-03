package com.example.finito.features.subtasks.data.dao

import androidx.room.*
import com.example.finito.features.subtasks.domain.entity.Subtask

@Dao
interface SubtaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createMany(vararg subtasks: Subtask)

    @Update
    suspend fun updateMany(vararg subtasks: Subtask)

    @Delete
    suspend fun removeMany(vararg subtasks: Subtask)
}