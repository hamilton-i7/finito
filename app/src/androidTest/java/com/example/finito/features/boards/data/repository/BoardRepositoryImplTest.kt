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
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
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
class BoardRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: FinitoDatabase
    private lateinit var boardRepositoryImpl: BoardRepositoryImpl
    private lateinit var dummyBoards: List<Board>

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
        dummyBoards = db.boardDao.findAll()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun should_add_board_into_list_when_asked() = runTest {
        assertThat(db.boardDao.findAll().size).isEqualTo(dummyBoards.size)

        val board = Board(name = "Board name")
        for (i in 0..2) {
            boardRepositoryImpl.create(board)
        }
        assertThat(db.boardDao.findAll().size).isEqualTo(dummyBoards.size + 3)
    }

    @Test
    fun should_return_board_id_when_successful_insert() = runTest {
        val board = Board(name = "Board name")
        val boardId = boardRepositoryImpl.create(board)
        assertThat(boardId).isEqualTo(dummyBoards.size + 1)
    }

    @Test
    fun should_return_active_boards_when_asked() = runTest {
        val boardId = dummyBoards.first { !it.deleted && !it.archived }.boardId
        listOf(
            Task(boardId = boardId, name = "Task name"),
            Task(boardId = boardId, name = "Task name"),
            Task(boardId = boardId, name = "Task name"),
        ).onEach { db.taskDao.create(it) }

        val boards = boardRepositoryImpl.findActiveBoards().first()
        assertThat(boards).isNotEmpty()
        // Check if there are only active boards
        assertThat(boards.all { !it.board.archived && !it.board.deleted }).isTrue()
        // Check if any board has labels
        assertThat(boards.any { it.labels.isNotEmpty() }).isTrue()
        // Check if any board has tasks
        assertThat(boards.any { it.tasks.isNotEmpty() }).isTrue()
    }

    @Test
    fun should_return_simple_boards_when_asked() = runTest {
        val boards = boardRepositoryImpl.findSimpleBoards().first()
        assertThat(boards.size).isGreaterThan(0)

        // Check if there are only active boards
        val boardIds = boards.map { it.boardId }
        val deletedBoards = dummyBoards.filter { it.deleted }.groupBy { it.boardId }
        boardIds.forEach {
            assertThat(deletedBoards[it]).isNull()
        }

        val archivedBoards = dummyBoards.filter { it.archived }.groupBy { it.boardId }
        boardIds.forEach {
            assertThat(archivedBoards[it]).isNull()
        }
    }

    @Test
    fun should_only_return_archived_boards_when_asked() = runTest {
        val archivedBoards = boardRepositoryImpl.findArchivedBoards().first()
        assertThat(archivedBoards.size).isGreaterThan(0)
        assertThat(archivedBoards.all { it.board.archived }).isTrue()
    }

    @Test
    fun should_only_return_deleted_boards_when_asked() = runTest {
        val deletedBoards = boardRepositoryImpl.findDeletedBoards().first()
        assertThat(deletedBoards.size).isGreaterThan(0)
        assertThat(deletedBoards.all { it.board.deleted }).isTrue()
    }

    @Test
    fun should_return_null_when_not_found() = runTest {
        val board = boardRepositoryImpl.findOne(10_000)
        assertThat(board).isNull()
    }

    @Test
    fun should_return_requested_board_when_found() = runTest {
        val board = boardRepositoryImpl.findOne(dummyBoards.random().boardId)
        assertThat(board).isNotNull()
    }

    @Test
    fun should_return_detailed_board_when_found() = runTest {
        val board = dummyBoards.random()
        listOf(
            Task(boardId = board.boardId, name = "Task name"),
            Task(boardId = board.boardId, name = "Task name"),
            Task(boardId = board.boardId, name = "Task name"),
        ).onEach { db.taskDao.create(it) }
        val taskIds = db.taskDao.findTasksByBoard(board.boardId)
        listOf(
            Subtask(taskId = taskIds.random().taskId, name = "Subtask name"),
            Subtask(taskId = taskIds.random().taskId, name = "Subtask name"),
            Subtask(taskId = taskIds.random().taskId, name = "Subtask name"),
        ).also { db.subtaskDao.createMany(*it.toTypedArray()) }

        val detailedBoard = boardRepositoryImpl.findOne(board.boardId)
        assertThat(detailedBoard).isNotNull()
        assertThat(detailedBoard?.tasks).isNotEmpty()
        assertThat(detailedBoard?.tasks?.map { it.subtasks }).isNotEmpty()
    }

    @Test
    fun should_update_board_when_asked() = runTest {
        val board = dummyBoards.random()
        assertThat(board.name).startsWith("Board")

        boardRepositoryImpl.update(board.copy(name = "Updated name"))
        assertThat(db.boardDao.findOne(board.boardId)?.board?.name).isEqualTo("Updated name")
    }

    @Test
    fun should_remove_board_from_list_when_asked() = runTest {
        val board = dummyBoards.random()
        assertThat(db.boardDao.findOne(board.boardId)).isNotNull()

        boardRepositoryImpl.remove(board)
        assertThat(db.boardDao.findOne(board.boardId)).isNull()
        assertThat(db.boardDao.findAll().size).isLessThan(dummyBoards.size)
    }
}