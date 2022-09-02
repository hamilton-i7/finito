package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.Priority
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.labels.domain.util.TaskOrder
import com.example.finito.features.tasks.data.repository.FakeTaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
class FindTodayTasksTest {
    private lateinit var findTodayTasks: FindTodayTasks
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
        findTodayTasks = FindTodayTasks(fakeTaskRepository)
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

        ('A'..'Z').forEachIndexed { index, c ->
            dummyTasks.add(
                Task(
                    taskId = index + 1,
                    name = "Task $c",
                    boardId = boards.random().boardId,
                    date = if (index % 2 == 0) dates.random() else null,
                    time = if (index % 4 == 0) time.random() else null,
                    priority = priorities.random()
                ),
            )
        }
        dummyTasks.shuffle()
        dummyTasks.forEach { fakeTaskRepository.create(it) }
    }

    @Test
    fun `find today tasks returns tasks with today's date only`() = runTest {
        val today = LocalDate.now()
        val tasks = findTodayTasks().first()
        assertThat(tasks).isNotEmpty()
        assertThat(tasks.all { it.task.date != null && it.task.date!!.isEqual(today) }).isTrue()
    }

    @Test
    fun `find today tasks returns list sorted by time descending`() = runTest {
        val tasks = findTodayTasks().first()

        for (i in 0..tasks.size - 2) {
            val currentTask = tasks[i].task
            val nextTask = tasks[i+1].task
            assertThat(
                isAfterOrEqual(currentTask.time, nextTask.time)
            ).isTrue()
        }
    }

    @Test
    fun `find today tasks returns list sorted by most urgent & time descending`() = runTest {
        val tasks = findTodayTasks(TaskOrder.MOST_URGENT).first()

        for (i in 0..tasks.size - 2) {
            val currentTask = tasks[i].task
            val nextTask = tasks[i+1].task
            if (currentTask.priority != null && nextTask.priority != null) {
                assertThat(
                    currentTask.priority!!.ordinal >= nextTask.priority!!.ordinal
                ).isTrue()
            } else if (currentTask.priority == nextTask.priority) {
                assertThat(
                    isAfterOrEqual(currentTask.time, nextTask.time)
                ).isTrue()
            }
        }
    }

    @Test
    fun `find today tasks returns list sorted by least urgent & time descending`() = runTest {
        val tasks = findTodayTasks(TaskOrder.LEAST_URGENT).first()

        for (i in 0..tasks.size - 2) {
            val currentTask = tasks[i].task
            val nextTask = tasks[i+1].task
            if (currentTask.priority != null && nextTask.priority != null) {
                assertThat(
                    currentTask.priority!!.ordinal <= nextTask.priority!!.ordinal
                ).isTrue()
            } else if (currentTask.priority == nextTask.priority) {
                assertThat(
                    isAfterOrEqual(currentTask.time, nextTask.time)
                ).isTrue()
            }
        }
    }

    private fun isAfterOrEqual(time1: LocalTime?, time2: LocalTime?): Boolean {
        if (time1 == null && time2 == null) return true
        if (time1 == null) {
            return false
        }
        if (time2 == null) {
            return true
        }
        return time1.isAfter(time2) || time1 == time2
    }
}