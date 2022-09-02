package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.util.BoardOrder
import com.example.finito.features.labels.domain.entity.SimpleLabel
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
    private lateinit var dummyBoards: MutableList<BoardWithLabels>

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        findAllBoards = FindAllBoards(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                BoardWithLabels(
                    board = Board(
                        boardId = index + 1,
                        name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                        archived = index % 3 == 0,
                        deleted = index % 2 == 0,
                        createdAt = LocalDateTime.now().plusMinutes(index.toLong())
                    ),
                    labels = if (index % 5 == 0) listOf(
                        SimpleLabel(labelId = index + 1, name = "Label name"),
                        SimpleLabel(labelId = index + 100, name = "Label name"),
                        SimpleLabel(labelId = index + 200, name = "Label name"),
                    ) else emptyList()
                )
            )
        }
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
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
    fun `Should return filtered boards when label IDs provided`() = runTest {
        val activeLabeledBoards = dummyBoards.filter {
            it.labels.isNotEmpty() && !it.board.deleted && !it.board.archived
        }
        val boards = findAllBoards().first()
        val filteredBoards = findAllBoards(
            labelIds = activeLabeledBoards.flatMap { it.labels }.map { it.labelId }.toIntArray()
        ).first()

        assertThat(boards).isNotEmpty()
        assertThat(activeLabeledBoards).isNotEmpty()
        assertThat(filteredBoards).isNotEmpty()
        assertThat(activeLabeledBoards.size).isEqualTo(filteredBoards.size)
        assertThat(boards.size).isGreaterThan(filteredBoards.size)
        assertThat(filteredBoards.any { it.board.archived && it.board.deleted }).isFalse()

        val filteredBoards2 = findAllBoards(
            labelIds = activeLabeledBoards.flatMap {
                it.labels
            }.take(1).map { it.labelId }.toIntArray()
        ).first()
        assertThat(filteredBoards2).isNotEmpty()
        assertThat(boards.size).isGreaterThan(filteredBoards2.size)
        assertThat(filteredBoards.size).isGreaterThan(filteredBoards2.size)

        val filteredBoards3 = findAllBoards(labelIds = intArrayOf()).first()
        assertThat(filteredBoards3.size).isEqualTo(boards.size)

        val inactiveLabeledBoards = dummyBoards.filter {
            it.labels.isNotEmpty() && it.board.deleted || it.board.archived
        }
        val filteredBoards5 = findAllBoards(
            labelIds = inactiveLabeledBoards.flatMap { it.labels }.map { it.labelId }.toIntArray()
        ).first()
        assertThat(inactiveLabeledBoards).isNotEmpty()
        assertThat(filteredBoards5).isEmpty()
    }
}
