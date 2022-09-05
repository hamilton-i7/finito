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
class UpdateTaskTest {
    private lateinit var updateTask: UpdateTask
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
        updateTask = UpdateTask(fakeTaskRepository, fakeSubtaskRepository)
        
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
                    name = "Task $c",
                    boardId = boardId,
                    date = if (index % 2 == 0) dates.random() else null,
                    time = if (index % 4 == 0) time.random() else null,
                    priority = priorities.random(),
                )
            )
        }
        dummyTasks.shuffle()
        dummyTasks.forEach { fakeTaskRepository.create(it) }
        fakeTaskRepository.findAll().map { it.taskId }.let {
            val subtasks = arrayOf(
                Subtask(taskId = it.random(), name = "Subtask name"),
                Subtask(taskId = it.random(), name = "Subtask name"),
                Subtask(taskId = it.random(), name = "Subtask name"),
            )
            fakeSubtaskRepository.createMany(*subtasks)
        }
    }

    @Test
    fun `Should throw EmptyException when task name is empty`() {
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest {
                TaskWithSubtasks(
                    task = fakeTaskRepository.findAll().random()
                ).let {
                    updateTask(it.copy(task = it.task.copy(name = "   ")))   
                }
            }
        }
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest {
                TaskWithSubtasks(
                    task = fakeTaskRepository.findAll().random()
                ).let {
                    updateTask(it.copy(task = it.task.copy(name = "")))
                }
            }
        }
    }

    @Test
    fun `Should throw InvalidStateException when task state is invalid`() {
        assertThrows(ResourceException.InvalidStateException::class.java) {
            runTest {
                TaskWithSubtasks(
                    task = fakeTaskRepository.findAll().random()
                ).let {
                    updateTask(it.copy(task = it.task.copy(
                        date = null,
                        time = LocalTime.now()
                    )))
                }
            }
        }
    }

    @Test
    fun `Should throw NotFoundException when no task is found`() {
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest {
                TaskWithSubtasks(
                    task = fakeTaskRepository.findAll().random()
                ).let {
                    updateTask(it.copy(task = it.task.copy(taskId = 10_000)))
                }
            }
        }
    }

    @Test
    fun `Should arrange tasks when switching boards`() = runTest {
        val tasks = fakeTaskRepository.findAll()
        val task = tasks.random()
        val newBoardId = fakeTaskRepository.findAll().first {
            it.boardId != task.boardId
        }.boardId

        var startBoardTasks = tasks.filter {
            it.boardId == task.boardId
        }.sortedBy { it.boardPosition }
        assertThat(startBoardTasks.find { it.taskId == task.taskId }).isNotNull()

        var endBoardTasks = tasks.filter {
            it.boardId == newBoardId
        }.sortedBy { it.boardPosition }
        assertThat(endBoardTasks.find { it.taskId == task.taskId }).isNull()

        updateTask(TaskWithSubtasks(task = task.copy(boardId = newBoardId)))

        with(fakeTaskRepository.findAll()) {
            startBoardTasks = filter {
                it.boardId == task.boardId
            }.sortedBy { it.boardPosition }
            assertThat(startBoardTasks.find { it.taskId == task.taskId }).isNull()

            endBoardTasks = filter {
                it.boardId == newBoardId
            }.sortedBy { it.boardPosition }
            assertThat(endBoardTasks.find { it.taskId == task.taskId }).isNotNull()

            // Check that every task in the start board is positioned correctly
            // [0, 1, 2, 3...]
            for (i in 0..startBoardTasks.size - 2) {
                assertThat(
                    startBoardTasks[i].boardPosition + 1 == startBoardTasks[i+1].boardPosition
                ).isTrue()
            }

            // Check that every task in the end board is positioned correctly
            // [0, 1, 2, 3...]
            for (i in 0..endBoardTasks.size - 2) {
                assertThat(
                    endBoardTasks[i].boardPosition + 1 == endBoardTasks[i+1].boardPosition
                ).isTrue()
            }

            first {
                it.taskId == task.taskId
            }.let {
                assertThat(it.boardPosition).isEqualTo(endBoardTasks.size - 1)
            }
        }
    }

    @Test
    fun `update task arranges tasks when switching positions`() = runTest {
        val tasks = fakeTaskRepository.findAll()
        val task = tasks.random()
        val position = tasks.filter {
            it.boardId == task.boardId
                    && it.boardPosition != task.boardPosition
        }.random().boardPosition
        updateTask(TaskWithSubtasks(task = task.copy(boardPosition = position)))

        with(fakeTaskRepository.findAll().filter {
            it.boardId == task.boardId
        }.sortedBy { it.boardPosition }) {
            assertThat(
                first { it.taskId == task.taskId }.boardPosition == position
            )

            for (i in 0..size - 2) {
                assertThat(
                    this[i].boardPosition + 1 == this[i+1].boardPosition
                ).isTrue()
            }
        }
    }

    @Test
    fun `Should delete subtasks when not included in task to update`() = runTest {
        val taskWithSubtasks = fakeTaskRepository.findOne(
            fakeSubtaskRepository.subtasks.random().taskId
        )!!

        fakeSubtaskRepository.findAllByTaskId(taskWithSubtasks.task.taskId).let {
            assertThat(it.size).isEqualTo(taskWithSubtasks.subtasks.size)
        }
        updateTask(
            taskWithSubtasks.copy(subtasks = emptyList())
        )
        fakeSubtaskRepository.findAllByTaskId(taskWithSubtasks.task.taskId).let {
            assertThat(it.size).isLessThan(taskWithSubtasks.subtasks.size)
        }
    }

    @Test
    fun `Should create subtasks when new subtasks were added`() = runTest {
        val taskWithSubtasks = fakeTaskRepository.findOne(
            fakeSubtaskRepository.subtasks.random().taskId
        )!!
        val newSubtasks = listOf(
            Subtask(name = "Subtask name", taskId = taskWithSubtasks.task.taskId),
            Subtask(name = "Subtask name", taskId = taskWithSubtasks.task.taskId),
        )
        updateTask(
            taskWithSubtasks.copy(subtasks = taskWithSubtasks.subtasks + newSubtasks)
        )
        fakeSubtaskRepository.findAllByTaskId(taskWithSubtasks.task.taskId).let {
            assertThat(it.size).isGreaterThan(taskWithSubtasks.subtasks.size)
        }
    }
}