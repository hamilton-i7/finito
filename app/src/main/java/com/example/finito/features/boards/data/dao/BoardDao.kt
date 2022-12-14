package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import kotlinx.coroutines.flow.Flow

private const val ACTIVE_BOARD_STATE = "'ACTIVE'"
private const val ARCHIVED_BOARD_STATE = "'ARCHIVED'"
private const val DELETED_BOARD_STATE = "'DELETED'"

@Dao
interface BoardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(board: Board): Long

    @Query("SELECT * FROM boards")
    suspend fun findAll(): List<Board>

    @Transaction
    @Query("SELECT * FROM boards WHERE state = $ACTIVE_BOARD_STATE")
    fun findActiveBoards(): Flow<List<BoardWithLabelsAndTasks>>

    @Query("SELECT board_id, name, normalized_name FROM boards WHERE state = $ACTIVE_BOARD_STATE")
    fun findSimpleBoards(): Flow<List<SimpleBoard>>

    @Transaction
    @Query("SELECT * FROM boards WHERE state = $ARCHIVED_BOARD_STATE")
    fun findArchivedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    @Transaction
    @Query("SELECT * FROM boards WHERE state = $DELETED_BOARD_STATE")
    fun findDeletedBoards(): Flow<List<BoardWithLabelsAndTasks>>

    @Query("SELECT * FROM boards WHERE state = $DELETED_BOARD_STATE")
    suspend fun findDeletedBoardsAsync(): List<Board>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM boards WHERE board_id = :id")
    suspend fun findOne(id: Int): DetailedBoard?

    @Update
    suspend fun update(vararg boards: Board)

    @Delete
    suspend fun remove(vararg boards: Board)
}