package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.util.BoardOrder
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
class FindAllBoardsTest {

    private lateinit var findAllBoards: FindAllBoards

    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository
    private lateinit var fakeLabelRepository: FakeLabelRepository

    @Before
    fun setUp() = runTest {
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        fakeLabelRepository = FakeLabelRepository()
        fakeBoardRepository = FakeBoardRepository(fakeLabelRepository, fakeBoardLabelRepository)
        findAllBoards = FindAllBoards(fakeBoardRepository)

        val dummyBoards = mutableListOf<Board>()
        val dummyLabels = listOf(
            Label(name = "Label name"),
            Label(name = "Label name"),
            Label(name = "Label name"),
        )

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                    archived = index % 5 == 0,
                    deleted = index % 7 == 0,
                    createdAt = LocalDateTime.now().plusMinutes(index.toLong())
                )
            )
        }
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
        dummyLabels.forEach { fakeLabelRepository.create(it) }

        val labelIds = fakeLabelRepository.findSimpleLabels().first().map { it.labelId }
        val boardIds = fakeBoardRepository.boards.map { it.boardId }

        boardIds.filter { it % 2 == 0 }.map {
            BoardLabelCrossRef(
                boardId = it,
                labelId = labelIds.random()
            )
        }.let { fakeBoardLabelRepository.create(*it.toTypedArray()) }
    }

    @Test
    fun `Should return active boards only when asked`() = runTest {
        val boards = findAllBoards().first()
        assertThat(boards).isNotEmpty()
        assertThat(boards.all { !it.board.archived && !it.board.deleted }).isTrue()
    }

    @Test
    fun `Should return boards sorted by name ascending when asked for A_Z sorting`() = runTest {
        val sortedBoards = findAllBoards(BoardOrder.A_Z).first()
        assertThat(sortedBoards).isNotEmpty()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `Should return boards sorted by name descending when asked for Z_A`() = runTest {
        val sortedBoards = findAllBoards(BoardOrder.Z_A).first()
        assertThat(sortedBoards).isNotEmpty()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isGreaterThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `Should return boards sorted by date ascending when asked for OLDEST`() = runTest {
        val sortedBoards = findAllBoards(BoardOrder.OLDEST).first()
        assertThat(sortedBoards).isNotEmpty()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.createdAt).isLessThan(sortedBoards[i+1].board.createdAt)
        }
    }

    @Test
    fun `Should return boards sorted by date descending when asked for NEWEST`() = runTest {
        val sortedBoards = findAllBoards(BoardOrder.NEWEST).first()
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
        val boards = findAllBoards().first()
        val filteredBoards = findAllBoards(labelIds = labelIds).first()

        assertThat(boards).isNotEmpty()
        assertThat(filteredBoards).isNotEmpty()
        assertThat(boards.size).isGreaterThan(filteredBoards.size)
        assertThat(filteredBoards.any { it.board.archived && it.board.deleted }).isFalse()

        val filteredBoards2 = findAllBoards(labelIds = labelIds.take(1).toIntArray()).first()
        assertThat(filteredBoards2).isNotEmpty()
        assertThat(boards.size).isGreaterThan(filteredBoards2.size)
        assertThat(filteredBoards.size).isGreaterThan(filteredBoards2.size)
    }
}
