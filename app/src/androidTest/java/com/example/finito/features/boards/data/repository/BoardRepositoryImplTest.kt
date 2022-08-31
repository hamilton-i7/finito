package com.example.finito.features.boards.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BoardRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: FinitoDatabase
    private lateinit var boardRepositoryImpl: BoardRepositoryImpl
    private lateinit var dummyBoards: List<BoardWithLabels>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, FinitoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        boardRepositoryImpl = BoardRepositoryImpl(db.boardDao)
    }

    @Before
    fun prepopulate() = runTest {
        ('A'..'J').forEachIndexed { index, c ->
            runBlocking {
                db.boardDao.create(
                    Board(
                        name = "Board $c",
                        archived = index % 3 == 0,
                        deleted = index % 2 == 0
                    )
                )
            }
        }
        dummyBoards = db.boardDao.findAll().first()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun create() = runTest {
        assertThat(db.boardDao.findAll().first().size).isEqualTo(dummyBoards.size)

        val board = Board(name = "Board name")
        for (i in 0..2) {
            boardRepositoryImpl.create(board)
        }
        assertThat(db.boardDao.findAll().first().size)
            .isEqualTo(dummyBoards.size + 3)
    }

    @Test
    fun findAll() = runTest {
        val boards = boardRepositoryImpl.findAll().first()
        assertThat(boards.size).isGreaterThan(0)
        // Check if there are only active boards
        assertThat(boards.all { !it.board.archived && !it.board.deleted }).isTrue()
    }

    @Test
    fun findSimpleBoards() = runTest {
        val boards = boardRepositoryImpl.findSimpleBoards().first()
        assertThat(boards.size).isGreaterThan(0)

        // Check if there are only active boards
        val boardIds = boards.map { it.boardId }
        val deletedBoards = dummyBoards.filter { it.board.deleted }.groupBy { it.board.boardId }
        boardIds.forEach {
            assertThat(deletedBoards[it]).isNull()
        }

        val archivedBoards = dummyBoards.filter { it.board.archived }.groupBy { it.board.boardId }
        boardIds.forEach {
            assertThat(archivedBoards[it]).isNull()
        }
    }

    @Test
    fun findArchivedBoards() = runTest {
        val archivedBoards = boardRepositoryImpl.findArchivedBoards().first()
        assertThat(archivedBoards.size).isGreaterThan(0)
        assertThat(archivedBoards.all { it.board.archived }).isTrue()
    }

    @Test
    fun findDeletedBoards() = runTest {
        val deletedBoards = boardRepositoryImpl.findDeletedBoards().first()
        assertThat(deletedBoards.size).isGreaterThan(0)
        assertThat(deletedBoards.all { it.board.deleted }).isTrue()
    }

    @Test
    fun findOne() = runTest {
        val board = boardRepositoryImpl.findOne(dummyBoards.random().board.boardId)
        assertThat(board).isNotNull()

        assertThat(boardRepositoryImpl.findOne(id = 0)).isNull()
    }
}