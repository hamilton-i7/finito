package com.example.finito.features.subtasks.data.dao

import androidx.room.*
import com.example.finito.features.subtasks.domain.entity.Subtask

@Dao
interface SubtaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createMany(vararg subtasks: Subtask)

    @Query("SELECT * FROM subtasks WHERE task_id = :taskId ORDER BY position ASC")
    suspend fun findAllByTaskId(taskId: Int): List<Subtask>

    @Query("SELECT * FROM subtasks WHERE subtask_id = :id")
    suspend fun findOne(id: Int): Subtask?

    @Update
    suspend fun updateMany(vararg subtasks: Subtask)

    @Delete
    suspend fun removeMany(vararg subtasks: Subtask)
}