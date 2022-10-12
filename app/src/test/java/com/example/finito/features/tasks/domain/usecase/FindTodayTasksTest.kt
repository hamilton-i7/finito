package com.example.finito.features.tasks.domain.usecase

import com.example.finito.core.domain.util.SortingOption
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
class FindTodayTasksTest {
    private lateinit var findTodayTasks: FindTodayTasks
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
        findTodayTasks = FindTodayTasks(fakeTaskRepository, fakeBoardRepository)

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
    fun `Should return today's tasks when asked`() = runTest {
        val today = LocalDate.now()
        val tasks = findTodayTasks().data.first()
        assertThat(tasks).isNotEmpty()
        assertThat(tasks.all { it.task.date != null && it.task.date!!.isEqual(today) }).isTrue()
    }

    @Test
    fun `Should return today's tasks sorted by time descending when asked`() = runTest {
        val tasks = findTodayTasks().data.first()

        for (i in 0..tasks.size - 2) {
            val currentTask = tasks[i].task
            val nextTask = tasks[i+1].task
            assertThat(
                isAfterOrEqual(currentTask.time, nextTask.time)
            ).isTrue()
        }
    }

    @Test
    fun `Should return today's tasks sorted by most urgent & time descending when asked for MOST_URGENT`() = runTest {
        val tasks = findTodayTasks(SortingOption.Priority.MostUrgent).data.first()

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
    fun `Should return today's tasks sorted by least urgent & time descending when asked for LEAST_URGENT`() = runTest {
        val tasks = findTodayTasks(SortingOption.Priority.LeastUrgent).data.first()

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