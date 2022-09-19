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
class FindActiveBoardsTest {

    private lateinit var findActiveBoards: FindActiveBoards

    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository
    private lateinit var fakeLabelRepository: FakeLabelRepository

    @Before
    fun setUp() = runTest {
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        fakeLabelRepository = FakeLabelRepository()
        fakeBoardRepository = FakeBoardRepository(fakeLabelRepository, fakeBoardLabelRepository)
        findActiveBoards = FindActiveBoards(fakeBoardRepository)

        val dummyBoards = mutableListOf<Board>()
        val dummyLabels = listOf(
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
    fun `Should return active boards only`() = runTest {
        val boards = findActiveBoards().data.first()
        assertThat(boards).isNotEmpty()
        assertThat(boards.all { it.board.state == BoardState.ACTIVE }).isTrue()
    }

    @Test
    fun `Should return boards sorted by name ascending when asked for A_Z sorting`() = runTest {
        val sortedBoards = findActiveBoards(SortingOption.Common.NameAZ).data.first()
        assertThat(sortedBoards).isNotEmpty()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `Should return boards sorted by name descending when asked for Z_A`() = runTest {
        val sortedBoards = findActiveBoards(SortingOption.Common.NameZA).data.first()
        assertThat(sortedBoards).isNotEmpty()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isGreaterThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `Should return boards sorted by date ascending when asked for OLDEST`() = runTest {
        val sortedBoards = findActiveBoards(SortingOption.Common.Oldest).data.first()
        assertThat(sortedBoards).isNotEmpty()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.createdAt).isLessThan(sortedBoards[i+1].board.createdAt)
        }
    }

    @Test
    fun `Should return boards sorted by date descending when asked for NEWEST`() = runTest {
        val sortedBoards = findActiveBoards(SortingOption.Common.Newest).data.first()
        assertThat(sortedBoards).isNotEmpty()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.createdAt).isGreaterThan(sortedBoards[i+1].board.createdAt)
        }
    }
    
    @Test
    fun `Should return filtered boards when label IDs are provided`() = runTest {
        val labelIds = fakeLabelRepository.findSimpleLabels().first().map {
            it.labelId
        }.toIntArray()
        val boards = findActiveBoards().data.first()
        val filteredBoards = findActiveBoards(labelIds = labelIds).data.first()

        assertThat(boards).isNotEmpty()
        assertThat(filteredBoards).isNotEmpty()
        assertThat(boards.size).isGreaterThan(filteredBoards.size)
        assertThat(filteredBoards.any {
            it.board.state == BoardState.ARCHIVED || it.board.state == BoardState.DELETED
        }).isFalse()

        val filteredBoards2 = findActiveBoards(labelIds = labelIds.take(1).toIntArray()).data.first()
        assertThat(filteredBoards2).isNotEmpty()
        assertThat(boards.size).isGreaterThan(filteredBoards2.size)
        assertThat(filteredBoards.size).isGreaterThan(filteredBoards2.size)
    }
}
