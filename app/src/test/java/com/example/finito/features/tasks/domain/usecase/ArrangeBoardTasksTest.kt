package com.example.finito.features.tasks.domain.usecase

import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.subtasks.data.repository.FakeSubtaskRepository
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.data.repository.FakeTaskRepository
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
class ArrangeBoardTasksTest {
    private lateinit var arrangeBoardTasks: ArrangeBoardTasks
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
        arrangeBoardTasks = ArrangeBoardTasks(fakeTaskRepository, fakeSubtaskRepository)

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
    fun `Should position completed tasks to end of list when completed`() = runTest {
        val boardId = boards.random().boardId
        var tasks = fakeTaskRepository.findTasksByBoard(boardId)
        assertThat(
            tasks.any { it.completed && it.boardId == tasks[0].boardId }
        ).isFalse()

        // Move completed tasks to the end of the list
        tasks.map {
            if (it.taskId % 2 == 0) {
                TaskWithSubtasks(task = it.copy(completed = true))
            } else {
                TaskWithSubtasks(task = it)
            }
        }.toMutableList().let {
            val completedTasks = it.filterCompleted()
            it.removeAll(completedTasks)
            it + completedTasks
        }.let { arrangeBoardTasks(it) }

        tasks = fakeTaskRepository.findTasksByBoard(boardId)
        assertThat(
            tasks.any { it.completed && it.boardId == tasks[0].boardId }
        ).isTrue()

        for (i in 0..tasks.size - 2) {
            assertThat(
                tasks[i].boardPosition + 1 == tasks[i+1].boardPosition
            ).isTrue()
        }

        // Check if completed tasks were moved to the end
        tasks.filter { it.taskId != tasks.last().taskId }.let {
            assertThat(
                it.all { task -> task.boardPosition < tasks.last().boardPosition }
            ).isTrue()
        }
        assertThat(tasks.last().completed).isTrue()
    }
}