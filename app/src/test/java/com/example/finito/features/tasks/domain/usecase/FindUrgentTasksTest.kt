package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.subtasks.data.repository.FakeSubtaskRepository
import com.example.finito.features.tasks.data.repository.FakeTaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.util.Priority
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
class FindUrgentTasksTest {
    private lateinit var findUrgentTasks: FindUrgentTasks

    private lateinit var fakeTaskRepository: FakeTaskRepository
    private lateinit var fakeSubtaskRepository: FakeSubtaskRepository
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var fakeLabelRepository: FakeLabelRepository
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository

    private lateinit var dummyBoards: List<Board>

    @Before
    fun setUp() = runTest {
        fakeSubtaskRepository = FakeSubtaskRepository()
        fakeTaskRepository = FakeTaskRepository(fakeSubtaskRepository)
        fakeLabelRepository = FakeLabelRepository()
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        fakeBoardRepository = FakeBoardRepository(fakeLabelRepository, fakeBoardLabelRepository)
        findUrgentTasks = FindUrgentTasks(fakeTaskRepository, fakeBoardRepository)

        listOf(
            Board(name = "Board name"),
            Board(name = "Board name"),
            Board(name = "Board name"),
        ).forEach { fakeBoardRepository.create(it) }

        dummyBoards = fakeBoardRepository.findAll()
        val dummyTasks = mutableListOf<Task>()

        val dates = listOf(
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(3),
            LocalDate.now().plusDays(4),
            LocalDate.now().plusMonths(2),
            LocalDate.now().plusMonths(3),
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
                    boardId = dummyBoards.random().boardId,
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
    fun `Should return urgent tasks when asked`() = runTest {
        val tasks = findUrgentTasks().data.first().values.flatten()
        assertThat(tasks).isNotEmpty()
        assertThat(tasks.all { it.task.priority == Priority.URGENT }).isTrue()
    }

    @Test
    fun `Should return tasks grouped by date descending when asked`() = runTest {
        val dates = findUrgentTasks().data.first().keys
        assertThat(dates).isNotEmpty()

        for (i in 0..dates.size - 2) {
            assertThat(
                isAfter(dates.elementAt(i)!!, dates.elementAt(index = i + 1))
            )
        }
    }

    @Test
    fun `Should return urgent tasks sorted by date & time descending when asked`() = runTest {
        val tasks = findUrgentTasks().data.first().values.flatten()
        assertThat(tasks).isNotEmpty()

        for (i in 0..tasks.size - 2) {
            val currentTask = tasks[i].task
            val nextTask = tasks[i+1].task

            if (currentTask.date == nextTask.date) {
                assertThat(
                    isAfterOrEqual(currentTask.time, nextTask.time)
                ).isTrue()
            }
            assertThat(
                isAfterOrEqual(currentTask.date, nextTask.date)
            ).isTrue()
        }
    }

    private fun isAfter(date1: LocalDate, date2: LocalDate?): Boolean {
        if (date2 == null) return true
        return date1.isAfter(date2)
    }

    private fun isAfterOrEqual(date1: LocalDate?, date2: LocalDate?): Boolean {
        if (date1 == null && date2 == null) return true
        if (date1 == null) {
            return false
        }
        if (date2 == null) {
            return true
        }
        return date1.isAfter(date2) || date1 == date2
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