package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.Priority
import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.subtasks.data.repository.FakeSubtaskRepository
import com.example.finito.features.subtasks.domain.entity.SimpleSubtask
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
    private lateinit var dummyTasks: MutableList<TaskWithSubtasks>

    private val boards = listOf(
        Board(boardId = 1, name = "Board name"),
        Board(boardId = 2, name = "Board name"),
        Board(boardId = 3, name = "Board name"),
    )

    @Before
    fun setUp() = runTest {
        fakeTaskRepository = FakeTaskRepository()
        fakeSubtaskRepository = FakeSubtaskRepository()
        updateTask = UpdateTask(fakeTaskRepository, fakeSubtaskRepository)
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

        var subtaskId = 1

        ('A'..'Z').forEachIndexed { index, c ->
            val boardId = boards.random().boardId
            dummyTasks.add(
                TaskWithSubtasks(
                    task = Task(
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
                    subtasks = if (index % 3 == 0) emptyList() else listOf(
                        SimpleSubtask(subtaskId = subtaskId, name = "Subtask name"),
                        SimpleSubtask(subtaskId = subtaskId + 1, name = "Subtask name"),
                        SimpleSubtask(subtaskId = subtaskId + 2, name = "Subtask name"),
                    )
                )
            )
            subtaskId++
        }
        dummyTasks.shuffle()
        dummyTasks.forEach { fakeTaskRepository.create(it) }
    }

    @Test
    fun `Should throw EmptyException when task name is empty`() {
        dummyTasks.random().let { taskWithSubtasks ->
            assertThrows(ResourceException.EmptyException::class.java) {
                runTest {
                    updateTask(
                        taskWithSubtasks.copy(task = taskWithSubtasks.task.copy(name = "   "))
                    )
                }
            }

            assertThrows(ResourceException.EmptyException::class.java) {
                runTest {
                    updateTask(
                        taskWithSubtasks.copy(task = taskWithSubtasks.task.copy(name = ""))
                    )
                }
            }
        }
    }

    @Test
    fun `Should throw InvalidStateException when task state is invalid`() {
        dummyTasks.random().let { taskWithSubtasks ->
            assertThrows(ResourceException.InvalidStateException::class.java) {
                runTest {
                    updateTask(
                        taskWithSubtasks.copy(
                            task = taskWithSubtasks.task.copy(
                                date = null,
                                time = LocalTime.now()
                            )
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `Should throw NotFoundException when no task is found`() {
        dummyTasks.random().let { taskWithSubtasks ->
            assertThrows(ResourceException.NotFoundException::class.java) {
                runTest {
                    updateTask(
                        taskWithSubtasks.copy(
                            task = taskWithSubtasks.task.copy(taskId = 10_000)
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `Should arrange tasks when switching boards`() = runTest {
        val taskWithSubtasks = dummyTasks.random()
        val newBoardId = dummyTasks.first {
            it.task.boardId != taskWithSubtasks.task.boardId
        }.task.boardId
        updateTask(
            taskWithSubtasks.copy(
                task = taskWithSubtasks.task.copy(boardId = newBoardId)
            )
        )

        with(fakeTaskRepository.findAll()) {
            val startBoardTasks = filter {
                it.task.boardId == taskWithSubtasks.task.boardId
            }.sortedBy { it.task.position }
            val endBoardTasks = filter {
                it.task.boardId == newBoardId
            }.sortedBy { it.task.position }

            assertThat(startBoardTasks.find {
                it.task.taskId == taskWithSubtasks.task.taskId
            }).isNull()
            assertThat(endBoardTasks.find {
                it.task.taskId == taskWithSubtasks.task.taskId }).isNotNull()

            dummyTasks.filter {
                it.task.boardId == taskWithSubtasks.task.boardId
            }.let {
                assertThat(startBoardTasks.size).isEqualTo(it.size - 1)
            }

            dummyTasks.filter {
                it.task.boardId == newBoardId
            }.let {
                assertThat(endBoardTasks.size).isEqualTo(it.size + 1)
            }

            // Check that every task in the start board is positioned correctly
            // [0, 1, 2, 3...]
            for (i in 0..startBoardTasks.size - 2) {
                assertThat(
                    startBoardTasks[i].task.position + 1 == startBoardTasks[i+1].task.position
                ).isTrue()
            }

            // Check that every task in the end board is positioned correctly
            // [0, 1, 2, 3...]
            for (i in 0..endBoardTasks.size - 2) {
                assertThat(
                    endBoardTasks[i].task.position + 1 == endBoardTasks[i+1].task.position
                ).isTrue()
            }

            first {
                it.task.taskId == taskWithSubtasks.task.taskId
            }.let {
                assertThat(it.task.position).isEqualTo(endBoardTasks.size - 1)
            }
        }
    }

    @Test
    fun `update task arranges tasks when switching positions`() = runTest {
        val taskWithSubtasks = dummyTasks.random()
        val position = dummyTasks.filter {
            it.task.boardId == taskWithSubtasks.task.boardId
                    && it.task.position != taskWithSubtasks.task.position
        }.random().task.position
        updateTask(
            taskWithSubtasks.copy(
                task = taskWithSubtasks.task.copy(position = position)
            )
        )

        with(fakeTaskRepository.findAll().filter {
            it.task.boardId == taskWithSubtasks.task.boardId
        }.sortedBy { it.task.position }) {
            assertThat(
                first { it.task.taskId == taskWithSubtasks.task.taskId }.task.position == position
            )

            for (i in 0..size - 2) {
                assertThat(
                    this[i].task.position + 1 == this[i+1].task.position
                ).isTrue()
            }
        }
    }
}