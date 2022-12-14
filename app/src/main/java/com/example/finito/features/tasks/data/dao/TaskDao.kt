package com.example.finito.features.tasks.data.dao

import androidx.room.*
import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(task: Task): Long

    @Query("SELECT * FROM tasks")
    suspend fun findAll(): List<Task>

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

    @Query("SELECT COUNT(*) FROM tasks WHERE board_id = :boardId")
    suspend fun findTasksByBoardAmount(boardId: Int): Int

    @Query("SELECT * FROM tasks WHERE board_id = :boardId ORDER BY board_position ASC")
    suspend fun findTasksByBoard(boardId: Int): List<Task>

    @Transaction
    @Query("SELECT * FROM tasks WHERE task_id = :id")
    suspend fun findOne(id: Int): TaskWithSubtasks?

    @Update
    suspend fun update(task: Task)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateMany(vararg tasks: Task)

    @Delete
    suspend fun remove(vararg tasks: Task)
}