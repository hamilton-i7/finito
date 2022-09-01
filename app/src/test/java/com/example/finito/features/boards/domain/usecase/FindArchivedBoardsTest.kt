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
class FindArchivedBoardsTest {

    private lateinit var findArchivedBoards: FindArchivedBoards
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<BoardWithLabels>

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        findArchivedBoards = FindArchivedBoards(fakeBoardRepository)
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
    fun `find archived boards returns archived boards only`() = runTest {
        val boards = findArchivedBoards().first()
        assertThat(boards.all { it.board.archived }).isTrue()
    }

    @Test
    fun `find archived boards returns list sorted by name ascending`() = runTest {
        val sortedBoards = findArchivedBoards(BoardOrder.A_Z).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `find archived boards returns list sorted by name descending`() = runTest {
        val sortedBoards = findArchivedBoards(BoardOrder.Z_A).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isGreaterThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `find archived boards returns list sorted by creation date ascending`() = runTest {
        val sortedBoards = findArchivedBoards(BoardOrder.OLDEST).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isBefore(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }

    @Test
    fun `find archived boards returns list sorted by creation date descending`() = runTest {
        val sortedBoards = findArchivedBoards(BoardOrder.NEWEST).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isAfter(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }

    @Test
    fun `find archived boards returns filtered list`() = runTest {
        val archiveLabeledBoards = dummyBoards.filter {
            it.labels.isNotEmpty() && it.board.archived
        }
        val boards = findArchivedBoards().first()
        val filteredBoards = findArchivedBoards(
            labelIds = archiveLabeledBoards.flatMap { it.labels }.map { it.labelId }.toIntArray()
        ).first()

        assertThat(boards).isNotEmpty()
        assertThat(archiveLabeledBoards).isNotEmpty()
        assertThat(filteredBoards).isNotEmpty()
        assertThat(archiveLabeledBoards.size).isEqualTo(filteredBoards.size)
        assertThat(boards.size).isGreaterThan(filteredBoards.size)
        assertThat(filteredBoards.all { it.board.archived }).isTrue()

        val filteredBoards2 = findArchivedBoards(
            labelIds = archiveLabeledBoards.flatMap {
                it.labels
            }.take(1).map { it.labelId }.toIntArray()
        ).first()
        assertThat(filteredBoards2).isNotEmpty()
        assertThat(boards.size).isGreaterThan(filteredBoards2.size)
        assertThat(filteredBoards.size).isGreaterThan(filteredBoards2.size)

        val filteredBoards3 = findArchivedBoards(labelIds = intArrayOf()).first()
        assertThat(filteredBoards3.size).isEqualTo(boards.size)

        val activeLabeledBoards = dummyBoards.filter {
            it.labels.isNotEmpty() && !it.board.deleted && !it.board.archived
        }
        val filteredBoards5 = findArchivedBoards(
            labelIds = activeLabeledBoards.flatMap { it.labels }.map { it.labelId }.toIntArray()
        ).first()
        assertThat(activeLabeledBoards).isNotEmpty()
        assertThat(filteredBoards5).isEmpty()
    }
}
