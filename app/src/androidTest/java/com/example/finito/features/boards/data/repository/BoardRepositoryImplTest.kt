package com.example.finito.features.boards.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.domain.entity.Board
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
    fun prepopulate() {
        ('A'..'J').forEach {
            runBlocking {
                val board = Board(name = "Board $it")
                boardRepositoryImpl.create(board)
            }
        }
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun create() = runTest {
        val boards = db.boardDao.findAll().first()
        val board = Board(name = "Board name")

        assertThat(boards.size).isEqualTo(boards.size)
        for (i in 0..2) {
            boardRepositoryImpl.create(board)
        }
        assertThat(db.boardDao.findAll().first().size)
            .isEqualTo(boards.size + 3)
    }

    @Test
    fun createBoardWithLabels() = runTest {

    }

    @Test
    fun findAll() = runTest {
        val boards = boardRepositoryImpl.findAll().first()
        assertThat(boards.size).isGreaterThan(0)
    }
}