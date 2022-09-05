package com.example.finito.features.tasks.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.finito.core.Priority
import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.toTaskUpdate
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TaskRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: FinitoDatabase
    private lateinit var taskRepositoryImpl: TaskRepositoryImpl

    private lateinit var dummyBoards: List<Board>
    private lateinit var dummyTasks: List<Task>

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, FinitoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskRepositoryImpl = TaskRepositoryImpl(db.taskDao)
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
        dummyBoards = db.boardDao.findAll()

        val dates = listOf(
            LocalDate.now(),
            LocalDate.now().plusDays(1),
        )
        val time = listOf(
            LocalTime.now(),
            LocalTime.now().plusMinutes(5),
            LocalTime.now().plusMinutes(25),
            LocalTime.now().plusHours(1),
            LocalTime.now().plusHours(2),
        )
        val priorities = setOf(
            Priority.URGENT,
            Priority.MEDIUM,
            Priority.LOW,
            null
        )

        ('A'..'Z').forEachIndexed { index, c ->
            db.taskDao.create(
                Task(
                    name = "Task $c",
                    boardId = dummyBoards.map { it.boardId }.random(),
                    date = if (index % 2 == 0) dates.random() else null,
                    time = if (index % 4 == 0) time.random() else null,
                    priority = priorities.random()
                ),
            )
        }
        dummyTasks = db.taskDao.findAll()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun should_insert_task_into_list() = runTest {
        assertThat(db.taskDao.findAll().size).isEqualTo(dummyTasks.size)
        val task = Task(name = "New task", boardId = dummyBoards.random().boardId)
        for (i in 0..2) {
            taskRepositoryImpl.create(task)
        }
        assertThat(db.taskDao.findAll().size).isEqualTo(dummyTasks.size + 3)
    }

    @Test
    fun should_only_return_today_tasks() = runTest {
        val today = LocalDate.now()
        val tasks = taskRepositoryImpl.findTodayTasks().first()
        assertThat(tasks).isNotEmpty()
        assertThat(tasks.all { it.task.date != null && it.task.date!!.isEqual(today) }).isTrue()
    }

    @Test
    fun should_only_return_tomorrow_tasks() = runTest {
        val tomorrow = LocalDate.now().plusDays(1)
        val tasks = taskRepositoryImpl.findTomorrowTasks().first()
        assertThat(tasks).isNotEmpty()
        assertThat(tasks.all { it.task.date != null && it.task.date!!.isEqual(tomorrow) }).isTrue()
    }

    @Test
    fun should_only_return_urgent_tasks() = runTest {
        val tasks = taskRepositoryImpl.findUrgentTasks().first()
        assertThat(tasks).isNotEmpty()
        assertThat(tasks.all { it.task.priority == Priority.URGENT }).isTrue()
    }

    @Test
    fun should_return_related_tasks_by_board_when_board_id_provided() = runTest {
        val boardId = dummyTasks.random().boardId
        val tasks = taskRepositoryImpl.findTasksByBoard(boardId)
        assertThat(tasks).isNotEmpty()
        assertThat(tasks.all { it.boardId == boardId }).isTrue()
    }

    @Test
    fun should_return_null_when_no_task_found() = runTest {
        assertThat(taskRepositoryImpl.findOne(10_000)).isNull()
    }

    @Test
    fun should_return_requested_task_when_found() = runTest {
        assertThat(taskRepositoryImpl.findOne(dummyTasks.random().taskId)).isNotNull()
    }

    @Test
    fun should_update_requested_task() = runTest {
        val task = dummyTasks.random()
        assertThat(task.name).startsWith("Task")

        taskRepositoryImpl.update(task.copy(name = "Updated name").toTaskUpdate())
        assertThat(db.taskDao.findOne(task.taskId)?.task?.name).isEqualTo("Updated name")
    }

    @Test
    fun should_remove_task_from_list() = runTest {
        val task = dummyTasks.random()
        assertThat(db.taskDao.findOne(task.taskId)).isNotNull()

        taskRepositoryImpl.remove(task)
        assertThat(db.taskDao.findOne(task.taskId)).isNull()
        assertThat(db.taskDao.findAll().size).isLessThan(dummyTasks.size)
    }
}