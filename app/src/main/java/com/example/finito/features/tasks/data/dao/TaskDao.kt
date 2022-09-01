package com.example.finito.features.tasks.data.dao

import androidx.room.*
import com.example.finito.core.Priority
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(task: Task)

    @Transaction
    @Query("SELECT * FROM tasks " +
            "WHERE DATE(date) >= DATE() AND DATE(date) < DATE('now', '+1 day')")
    fun findTodayTasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks " +
            "WHERE DATE(date) >= DATE('now', '+1 day') AND DATE(date) < DATE('now', '+2 day')")
    fun findTomorrowTasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE priority = :priority")
    fun findUrgentTasks(priority: Priority = Priority.URGENT): Flow<List<TaskWithSubtasks>>

    @Query("SELECT COUNT(*) FROM tasks WHERE board_id = :boardId AND completed = 0")
    suspend fun findTasksByBoardAmount(boardId: Int): Int

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun remove(task: Task)
}