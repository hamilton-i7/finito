package com.example.finito.features.boards.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.labels.domain.entity.Label
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
                val id = index + 1
                db.boardDao.create(Board(boardId = id, name = "Board $c"))
                db.labelDao.create(Label(labelId = id, name = "Label $c"))

                if (index % 2 == 0) {
                    db.boardLabelDao.create(BoardLabelCrossRef(labelId = id, boardId = id))
                }
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
    }
}