package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.Priority
import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.tasks.data.repository.FakeTaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
class UpdateTaskTest {
    private lateinit var updateTask: UpdateTask
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
        updateTask = UpdateTask(fakeTaskRepository)
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
    fun `update task throws Exception if invalid name`() {
        dummyTasks.random().let { task ->
            assertThrows(ResourceException.EmptyException::class.java) {
                runTest { updateTask(task.copy(name = "   ")) }
            }

            assertThrows(ResourceException.EmptyException::class.java) {
                runTest { updateTask(task.copy(name = "")) }
            }
        }
    }

    @Test
    fun `update task throws Exception if invalid state`() {
        dummyTasks.random().let { task ->
            assertThrows(ResourceException.InvalidStateException::class.java) {
                runTest {
                    updateTask(
                        task.copy(
                            date = null,
                            time = LocalTime.now()
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `update task throws Exception if task is not found`() {
        dummyTasks.random().copy(taskId = dummyTasks.size + 1).let { task ->
            assertThrows(ResourceException.NotFoundException::class.java) {
                runTest { updateTask(task) }
            }
        }
    }

    @Test
    fun `update task arranges tasks when switching boards`() = runTest {
        val task = dummyTasks.random()
        val newBoardId = dummyTasks.first { it.boardId != task.boardId }.boardId
        updateTask(task.copy(boardId = newBoardId))

        with(fakeTaskRepository.findAll()) {
            val startBoardTasks = filter { it.boardId == task.boardId }.sortedBy { it.position }
            val endBoardTasks = filter { it.boardId == newBoardId }.sortedBy { it.position }

            assertThat(startBoardTasks.find { it.taskId == task.taskId }).isNull()
            assertThat(endBoardTasks.find { it.taskId == task.taskId }).isNotNull()

            dummyTasks.filter { it.boardId == task.boardId }.let {
                assertThat(startBoardTasks.size).isEqualTo(it.size - 1)
            }

            dummyTasks.filter { it.boardId == newBoardId }.let {
                assertThat(endBoardTasks.size).isEqualTo(it.size + 1)
            }

            // Check that every task in the start board is positioned correctly
            // [0, 1, 2, 3...]
            for (i in 0..startBoardTasks.size - 2) {
                assertThat(
                    startBoardTasks[i].position + 1 == startBoardTasks[i+1].position
                ).isTrue()
            }

            // Check that every task in the end board is positioned correctly
            // [0, 1, 2, 3...]
            for (i in 0..endBoardTasks.size - 2) {
                assertThat(
                    endBoardTasks[i].position + 1 == endBoardTasks[i+1].position
                ).isTrue()
            }

            first { it.taskId == task.taskId }.let {
                assertThat(it.position).isEqualTo(endBoardTasks.size - 1)
            }
        }
    }

    @Test
    fun `update task arranges tasks when switching positions`() = runTest {
        val task = dummyTasks.random()
        val position = dummyTasks.filter {
            it.boardId == task.boardId && it.position != task.position
        }.random().position
        updateTask(task.copy(position = position))

        with(fakeTaskRepository.findAll().filter {
            it.boardId == task.boardId
        }.sortedBy { it.position }) {
            assertThat(first { it.taskId == task.taskId }.position == position)

            for (i in 0..size - 2) {
                assertThat(
                    this[i].position + 1 == this[i+1].position
                ).isTrue()
            }
        }
    }
}