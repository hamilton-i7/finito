package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindArchivedBoardsTest {

    private lateinit var findArchivedBoards: FindArchivedBoards

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
        findArchivedBoards = FindArchivedBoards(fakeBoardRepository)
        dummyBoards = mutableListOf()
        dummyLabels = listOf(
            Label(name = "Label name"),
            Label(name = "Label name"),
            Label(name = "Label name"),
        )

        dummyBoards.addAll(Board.dummyBoards)
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
        dummyLabels.forEach { fakeLabelRepository.create(it) }

        val labelIds = fakeLabelRepository.findSimpleLabels().first().map { it.labelId }
        val boardIds = fakeBoardRepository.findAll().map { it.boardId }

        boardIds.filter { it % 2 == 0 }.map {
            BoardLabelCrossRef(
                boardId = it,
                labelId = labelIds.random()
            )
        }.let { fakeBoardLabelRepository.create(*it.toTypedArray()) }
    }

    @Test
    fun `Should return archived boards only`() = runTest {
        val boards = findArchivedBoards().first()
        assertThat(boards.all { it.board.state == BoardState.ARCHIVED }).isTrue()
    }

    @Test
    fun `Should return boards sorted by name ascending when asked for A_Z sorting`() = runTest {
        val sortedBoards = findArchivedBoards(SortingOption.Common.NameAZ).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `Should return boards sorted by name descending when asked for Z_A sorting`() = runTest {
        val sortedBoards = findArchivedBoards(SortingOption.Common.NameZA).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isGreaterThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `Should return boards sorted by date ascending when asked for OLDEST`() = runTest {
        val sortedBoards = findArchivedBoards(SortingOption.Common.Oldest).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isBefore(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }

    @Test
    fun `Should return boards sorted by date descending when asked for NEWEST`() = runTest {
        val sortedBoards = findArchivedBoards(SortingOption.Common.Newest).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isAfter(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }

    @Test
    fun `Should return filtered boards when label IDs provided`() = runTest {
        val archiveLabeledBoards = findArchivedBoards().first().filter { it.labels.isNotEmpty() }
        val boards = findArchivedBoards().first()
        val filteredBoards = findArchivedBoards(
            labelIds = archiveLabeledBoards.flatMap { it.labels }.map { it.labelId }.toIntArray()
        ).first()

        assertThat(boards).isNotEmpty()
        assertThat(archiveLabeledBoards).isNotEmpty()
        assertThat(filteredBoards).isNotEmpty()
        assertThat(archiveLabeledBoards.size).isEqualTo(filteredBoards.size)
        assertThat(boards.size).isGreaterThan(filteredBoards.size)
        assertThat(filteredBoards.all { it.board.state == BoardState.ARCHIVED }).isTrue()

        val filteredBoards2 = findArchivedBoards(
            labelIds = archiveLabeledBoards.flatMap {
                it.labels
            }.take(1).map { it.labelId }.toIntArray()
        ).first()
        assertThat(filteredBoards2).isNotEmpty()
        assertThat(boards.size).isGreaterThan(filteredBoards2.size)
        assertThat(filteredBoards.size >= filteredBoards2.size).isTrue()

        val filteredBoards3 = findArchivedBoards(labelIds = intArrayOf()).first()
        assertThat(filteredBoards3.size).isEqualTo(boards.size)
    }
}
