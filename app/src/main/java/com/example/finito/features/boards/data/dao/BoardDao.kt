package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(board: Board): Long

    @Query("SELECT * FROM boards")
    suspend fun findAll(): List<Board>

    @Transaction
    @Query("SELECT * FROM boards WHERE archived = 0 AND deleted = 0")
    fun findActiveBoards(): Flow<List<BoardWithLabelsAndTasks>>

    @Query("SELECT board_id, name, normalized_name FROM boards WHERE archived = 0 AND deleted = 0")
    fun findSimpleBoards(): Flow<List<SimpleBoard>>

    @Transaction
    @Query("SELECT * FROM boards WHERE archived = 1")
    fun findArchivedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    @Transaction
    @Query("SELECT * FROM boards WHERE deleted = 1")
    fun findDeletedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM boards WHERE board_id = :id")
    suspend fun findOne(id: Int): DetailedBoard?

    @Update
    suspend fun update(board: Board)

    @Delete
    suspend fun remove(vararg boards: Board)
}