package com.example.finito.features.boards.data.dao

import androidx.room.*
import com.example.finito.features.boards.domain.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(board: Board)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(board: Board, labels: List<BoardLabelCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(vararg labels: BoardLabelCrossRef)

    @Transaction
    @Query("SELECT * FROM boards")
    fun findAll(): Flow<List<BoardWithLabels>>

    @Query("SELECT board_id, name, normalized_name FROM boards")
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

    @Update
    suspend fun update(board: Board)

    @Delete
    suspend fun remove(board: Board)

    @Delete
    suspend fun remove(vararg labels: BoardLabelCrossRef)
}