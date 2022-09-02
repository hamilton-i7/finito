package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(board: Board)

    @Transaction
    @Query("SELECT * FROM boards WHERE archived = 0 AND deleted = 0")
    fun findAll(): Flow<List<BoardWithLabels>>

    @Query("SELECT board_id, name, normalized_name FROM boards WHERE archived = 0 AND deleted = 0")
    fun findSimpleBoards(): Flow<List<SimpleBoard>>

    @Transaction
    @Query("SELECT * FROM boards WHERE archived = 1")
    fun findArchivedBoards(): Flow<List<BoardWithLabels>>

    @Transaction
    @Query("SELECT * FROM boards WHERE deleted = 1")
    fun findDeletedBoards(): Flow<List<BoardWithLabels>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM boards WHERE board_id = :id")
    suspend fun findOne(id: Int): DetailedBoard?

    @Query("SELECT seq FROM sqlite_sequence WHERE name = 'boards'")
    suspend fun findNewestId(): Int

    @Update
    suspend fun update(board: Board): Int

    @Delete
    suspend fun remove(board: Board): Int
}