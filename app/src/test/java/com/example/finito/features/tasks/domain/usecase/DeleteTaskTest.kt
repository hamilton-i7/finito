package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.Priority
import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.subtasks.data.repository.FakeSubtaskRepository
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
class DeleteTaskTest {
    private lateinit var deleteTask: DeleteTask
    private lateinit var fakeTaskRepository: FakeTaskRepository
    private lateinit var fakeSubtaskRepository: FakeSubtaskRepository

    private val boards = listOf(
        Board(boardId = 1, name = "Board name"),
        Board(boardId = 2, name = "Board name"),
        Board(boardId = 3, name = "Board name"),
    )

    @Before
    fun setUp() = runTest {
        fakeSubtaskRepository = FakeSubtaskRepository()
        fakeTaskRepository = FakeTaskRepository(fakeSubtaskRepository)
        deleteTask = DeleteTask(fakeTaskRepository)

        val dummyTasks = mutableListOf<Task>()

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
            val boardId = boards.random().boardId
            dummyTasks.add(
                Task(
                    taskId = index + 1,
                    name = "Task $c",
                    boardId = boardId,
                    date = if (index % 2 == 0) dates.random() else null,
                    time = if (index % 4 == 0) time.random() else null,
                    priority = priorities.random(),
                ),
            )
        }
        dummyTasks.shuffle()
        dummyTasks.forEach { fakeTaskRepository.create(it) }
    }

    @Test
    fun `Should throw NegativeIdException when ID is invalid`() {
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest {
                fakeTaskRepository.findAll().random().copy(taskId = 0).let {
                    deleteTask(it)
                }
            }
        }
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest {
                fakeTaskRepository.findAll().random().copy(taskId = -2).let {
                    deleteTask(it)
                }
            }
        }
    }

    @Test
    fun `Should throw NotFoundException when task isn't found`() {
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest {
                fakeTaskRepository.findAll().random().copy(taskId = 10_000).let {
                    deleteTask(it)
                }
            }
        }
    }

    @Test
    fun `Should remove task from the list when it is found`() = runTest {
        val tasksAmount = fakeTaskRepository.findAll().size
        val taskToDelete = fakeTaskRepository.findAll().random()

        deleteTask(taskToDelete)
        fakeTaskRepository.findAll().let {
            assertThat(it.find { task -> task.taskId == taskToDelete.taskId }).isNull()
            assertThat(it.size).isLessThan(tasksAmount)
        }
    }

    @Test
    fun `Should arrange remaining tasks when task is found`() = runTest {
        val task = fakeTaskRepository.findAll().random()
        deleteTask(task)
        with(fakeTaskRepository.findTasksByBoard(task.boardId)) {
            for (i in 0..size - 2) {
                assertThat(
                    this[i].boardPosition + 1 == this[i+1].boardPosition
                ).isTrue()
            }
        }
    }
}