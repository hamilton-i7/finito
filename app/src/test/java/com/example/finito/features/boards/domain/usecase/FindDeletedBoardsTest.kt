package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class FindDeletedBoardsTest {

    private lateinit var findDeletedBoards: FindDeletedBoards

    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository
    private lateinit var fakeLabelRepository: FakeLabelRepository

    private lateinit var dummyBoards: MutableList<Board>
    private lateinit var dummyLabels: List<Label>

    @Before
    fun setUp() = runTest {
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        fakeLabelRepository = FakeLabelRepository()
        fakeBoardRepository = FakeBoardRepository(fakeLabelRepository, fakeBoardLabelRepository)
        findDeletedBoards = FindDeletedBoards(fakeBoardRepository)
        dummyBoards = mutableListOf()
        dummyLabels = listOf(
            Label(name = "Label name"),
            Label(name = "Label name"),
            Label(name = "Label name"),
        )

        val timestamps = listOf(
            LocalDateTime.now(),
            LocalDateTime.now().plusYears(2),
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(2),
            LocalDateTime.now().minusMonths(1),
            LocalDateTime.now().plusWeeks(2),
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().plusMinutes(2),
        )

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                    archived = index % 3 == 0,
                    deleted = index % 2 == 0,
                    createdAt = LocalDateTime.now().plusMinutes(index.toLong()),
                    removedAt = if (index % 2 == 0) timestamps.random() else null
                )
            )
        }
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
        dummyLabels.forEach { fakeLabelRepository.create(it) }
        dummyBoards.filter { it.boardId % 3 == 0 }.map {
            BoardLabelCrossRef(
                boardId = it.boardId,
                labelId = dummyLabels.random().labelId
            )
        }.let { fakeBoardLabelRepository.create(*it.toTypedArray()) }
    }

    @Test
    fun `Should return deleted boards only when asked`() = runTest {
        val boards = findDeletedBoards().first()
        assertThat(boards.all { it.board.deleted }).isTrue()
    }

    @Test
    fun `Should return deleted boards sorted by removed date descending`() = runTest {
        val sortedBoards = findDeletedBoards().first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.removedAt?.isAfterOrEquals(sortedBoards[i+1].board.removedAt)
            ).isTrue()
        }
    }

    private fun LocalDateTime?.isAfterOrEquals(timestamp2: LocalDateTime?): Boolean {
        if (this == null && timestamp2 == null) return true
        if (this == null) return false
        if (timestamp2 == null) return true
        return isAfter(timestamp2) || isEqual(timestamp2)
    }
}
