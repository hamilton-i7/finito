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
class DeleteTaskTest {
    private lateinit var deleteTask: DeleteTask
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
        deleteTask = DeleteTask(fakeTaskRepository)
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
    fun `Should throw InvalidIdException when ID is invalid`() {
        dummyTasks.random().copy(taskId = 0).let {
            assertThrows(ResourceException.InvalidIdException::class.java) {
                runTest { deleteTask(it) }
            }
        }
        dummyTasks.random().copy(taskId = -2).let {
            assertThrows(ResourceException.InvalidIdException::class.java) {
                runTest { deleteTask(it) }
            }
        }
    }

    @Test
    fun `Should throw NotFoundException when task isn't found`() {
        val latestId = dummyTasks.map { it.taskId }.max()
        dummyTasks.random().copy(taskId = latestId + 1).let {
            assertThrows(ResourceException.NotFoundException::class.java) {
                runTest { deleteTask(it) }
            }
        }
    }

    @Test
    fun `Should remove task from the list when it is found`() = runTest {
        with(dummyTasks.random()) {
            deleteTask(task = this)

            fakeTaskRepository.findAll().let {
                assertThat(it.find { task -> task.taskId == taskId }).isNull()
                assertThat(it.size).isLessThan(dummyTasks.size)
            }
        }
    }

    @Test
    fun `Should arrange remaining tasks when task is found`() = runTest {
        val task = dummyTasks.random()
        deleteTask(task)
        with(fakeTaskRepository.findTasksByBoard(task.boardId)) {
            for (i in 0..size - 2) {
                assertThat(
                    this[i].position + 1 == this[i+1].position
                ).isTrue()
            }
        }
    }
}