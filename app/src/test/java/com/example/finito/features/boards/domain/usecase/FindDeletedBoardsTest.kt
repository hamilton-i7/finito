package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.labels.domain.entity.SimpleLabel
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
    private lateinit var dummyBoards: MutableList<BoardWithLabels>

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        findDeletedBoards = FindDeletedBoards(fakeBoardRepository)
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
    fun `find all boards returns deleted boards only`() = runTest {
        val boards = findDeletedBoards().first()
        assertThat(boards.all { it.board.deleted }).isTrue()
    }

    @Test
    fun `findDeletedBoards( returns list sorted by name ascending`() = runTest {
        val sortedBoards = findDeletedBoards().first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }
}
