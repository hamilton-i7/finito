package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.Priority
import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.subtasks.data.repository.FakeSubtaskRepository
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.data.repository.FakeTaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
class CreateTaskTest {
    private lateinit var createTask: CreateTask
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
        createTask = CreateTask(fakeTaskRepository, fakeSubtaskRepository)

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
                    boardPosition = when (boardId) {
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
        TaskWithSubtasks(
            task = Task(name = "", boardId = boards.random().boardId)
        ).let {
            assertThrows(ResourceException.EmptyException::class.java) {
                runTest { createTask(it) }
            }
        }
        TaskWithSubtasks(
            task = Task(name = "     ", boardId = boards.random().boardId)
        ).let {
            assertThrows(ResourceException.EmptyException::class.java) {
                runTest { createTask(it) }
            }
        }
    }

    @Test
    fun `Should throw InvalidStateException when task state is invalid`() {
        TaskWithSubtasks(
            task = Task(
                name = "Task name",
                boardId = boards.random().boardId,
                time = LocalTime.now()
            )
        ).let {
            assertThrows(ResourceException.InvalidStateException::class.java) {
                runTest { createTask(it) }
            }
        }
    }

    @Test
    fun `Should insert new task into list when task state is valid`() = runTest {
        val taskWithSubtasks = TaskWithSubtasks(
            task = Task(name = "Task name", boardId = boards.random().boardId)
        )
        with(fakeTaskRepository.findTasksByBoard(taskWithSubtasks.task.boardId)) {
            val tasksInBoard = fakeTaskRepository.findAll().filter {
                it.boardId == taskWithSubtasks.task.boardId
            }

            assertThat(size).isEqualTo(tasksInBoard.size)
            createTask(taskWithSubtasks)

            fakeTaskRepository.findTasksByBoard(taskWithSubtasks.task.boardId).let {
                assertThat(it.size).isEqualTo(tasksInBoard.size + 1)
            }
        }
    }

    @Test
    fun `Should set task position to list size when task state is valid`() = runTest {
        val taskWithSubtasks = TaskWithSubtasks(
            task = Task(name = "Task name", boardId = boards.random().boardId)
        )
        val tasks = fakeTaskRepository.findTasksByBoard(taskWithSubtasks.task.boardId)
        createTask(taskWithSubtasks)

        with(fakeTaskRepository.findTasksByBoard(taskWithSubtasks.task.boardId).first {
            it.name == "Task name"
        }) { assertThat(boardPosition).isEqualTo(tasks.size) }
    }

    @Test
    fun `Should create subtasks when task is created`() = runTest {
        val taskWithSubtasks = TaskWithSubtasks(
            task = Task(name = "Task name", boardId = boards.random().boardId),
            subtasks = listOf(
                Subtask(name = "Subtask name"),
                Subtask(name = "Subtask name"),
                Subtask(name = "Subtask name"),
            )
        )
        val taskId = createTask(taskWithSubtasks)
        assertThat(
            fakeSubtaskRepository.findAllByTaskId(taskId).size
        ).isEqualTo(taskWithSubtasks.subtasks.size)
    }
}