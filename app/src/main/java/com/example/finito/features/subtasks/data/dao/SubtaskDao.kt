package com.example.finito.features.subtasks.data.dao

import androidx.room.*
import com.example.finito.features.subtasks.domain.entity.Subtask

@Dao
interface SubtaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createMany(vararg subtasks: Subtask)

    @Query("SELECT * FROM subtasks WHERE task_id = :taskId")
    suspend fun findAllByTaskId(taskId: Int): List<Subtask>

    @Update
    suspend fun updateMany(vararg subtasks: Subtask): Int

    @Delete
    suspend fun removeMany(vararg subtasks: Subtask): Int
}