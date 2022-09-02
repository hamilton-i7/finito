package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.Priority
import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.tasks.data.repository.FakeTaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
class CreateTaskTest {
    private lateinit var createTask: CreateTask
    private lateinit var fakeTaskRepository: FakeTaskRepository
    private lateinit var dummyTasks: MutableList<Task>

    private val boards = listOf(
        Board(boardId = 1, name = "Board name"),
        Board(boardId = 2, name = "Board name"),
        Board(boardId = 3, name = "Board name"),
    )

    @Before
    fun setUp() = runTest {
        fakeTaskRepository = FakeTaskRepository()
        createTask = CreateTask(fakeTaskRepository)
        dummyTasks = mutableListOf()

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

        var tasksInBoard1Position = 0
        var tasksInBoard2Position = 0
        var tasksInBoard3Position = 0

        ('A'..'Z').forEachIndexed { index, c ->
            val boardId = boards.random().boardId
            dummyTasks.add(
                Task(
                    taskId = index + 1,
                    name = "Task $c",
                    boardId = boardId,
                    date = if (index % 2 == 0) dates.random() else null,
                    time = if (index % 4 == 0) time.random() else null,
                    priority = priorities.random(),
                    position = when (boardId) {
                        boards[0].boardId -> tasksInBoard1Position++
                        boards[1].boardId -> tasksInBoard2Position++
                        else -> tasksInBoard3Position++
                    }
                ),
            )
        }
        dummyTasks.shuffle()
        dummyTasks.forEach { fakeTaskRepository.create(it) }
    }

    @Test
    fun `Should throw EmptyException when task name is empty`() {
        Task(name = "", boardId = boards.random().boardId).let {
            Assert.assertThrows(ResourceException.EmptyException::class.java) {
                runTest { createTask(it) }
            }
        }

        Task(name = "     ", boardId = boards.random().boardId).let {
            Assert.assertThrows(ResourceException.EmptyException::class.java) {
                runTest { createTask(it) }
            }
        }
    }

    @Test
    fun `Should throw InvalidException when task state is invalid`() {
        Task(
            name = "Task name",
            boardId = boards.random().boardId,
            time = LocalTime.now()
        ).let {
            Assert.assertThrows(ResourceException.InvalidException::class.java) {
                runTest { createTask(it) }
            }
        }
    }

    @Test
    fun `Should insert new task into list when task state is valid`() = runTest {
        val task = Task(name = "Task name", boardId = boards.random().boardId)
        with(fakeTaskRepository.findTasksByBoard(task.boardId)) {
            val tasksInBoard = fakeTaskRepository.findAll().filter { it.boardId == task.boardId }

            assertThat(size).isEqualTo(tasksInBoard.size)
            createTask(task)

            fakeTaskRepository.findTasksByBoard(task.boardId).let {
                assertThat(it.size).isEqualTo(tasksInBoard.size + 1)
            }
        }
    }

    @Test
    fun `Should set task position to list size when task state is valid`() = runTest {
        val task = Task(name = "Task name", boardId = boards.random().boardId)
        val tasks = fakeTaskRepository.findTasksByBoard(task.boardId)
        createTask(task)

        with(fakeTaskRepository.findTasksByBoard(task.boardId).first {
            it.name == "Task name"
        }) { assertThat(position).isEqualTo(tasks.size) }
    }
}