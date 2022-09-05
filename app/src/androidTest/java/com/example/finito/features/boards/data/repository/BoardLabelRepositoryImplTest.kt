package com.example.finito.features.boards.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BoardLabelRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: FinitoDatabase
    private lateinit var boardLabelRepositoryImpl: BoardLabelRepositoryImpl
    private lateinit var refs: List<BoardLabelCrossRef>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, FinitoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        boardLabelRepositoryImpl = BoardLabelRepositoryImpl(db.boardLabelDao)
    }

    @Before
    fun prepopulate() = runTest {
        ('A'..'J').forEachIndexed { index, c ->
            db.boardDao.create(
                Board(
                    name = "Board $c",
                    archived = index % 3 == 0,
                    deleted = index % 2 == 0
                )
            )
        }
        ('A'..'C').forEach {
            db.labelDao.create(
                Label(name = "Label $it")
            )
        }
        val labelIds = db.labelDao.findSimpleLabels().first().map { it.labelId }
        val boardIds = db.boardDao.findAll().map { it.boardId }

        boardIds.filter { it % 2 == 0 }.map {
            BoardLabelCrossRef(
                boardId = it,
                labelId = labelIds.random()
            )
        }.let { db.boardLabelDao.create(*it.toTypedArray()) }
        refs = db.boardLabelDao.findAll()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun should_insert_refs_into_list() = runTest {
        val labelIds = db.labelDao.findSimpleLabels().first().map { it.labelId }
        val boardIds = db.boardDao.findAll().map { it.boardId }

        boardIds.filter { it % 7 == 0 }.take(2).map {
            BoardLabelCrossRef(
                boardId = it,
                labelId = labelIds.random()
            )
        }.let { db.boardLabelDao.create(*it.toTypedArray()) }
        assertThat(refs.size).isLessThan(boardLabelRepositoryImpl.findAll().size)
    }

    @Test
    fun should_return_refs_when_board_id_is_provided() = runTest {
        val boardId = db.boardLabelDao.findAll().random().boardId
        assertThat(boardLabelRepositoryImpl.findAllByBoardId(boardId)).isNotEmpty()
        assertThat(
            boardLabelRepositoryImpl.findAllByBoardId(boardId).size
        ).isLessThan(db.boardLabelDao.findAll().size)
    }

    @Test
    fun should_remove_ref_from_list() = runTest {
        db.boardLabelDao.findAll().take(3).also {
            boardLabelRepositoryImpl.remove(*it.toTypedArray())
        }
        assertThat(db.boardLabelDao.findAll().size).isEqualTo(refs.size - 3)
    }
}