package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.util.BoardOrder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindDeletedBoardsTest {

    private lateinit var findDeletedBoards: FindDeletedBoards
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        findDeletedBoards = FindDeletedBoards(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            runBlocking {
                delay(25L)
                dummyBoards.add(
                    Board(
                        boardId = index,
                        name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                        archived = index % 3 == 0,
                        deleted = index % 2 == 0
                    )
                )
            }
        }
        dummyBoards.shuffle()
        runBlocking {
            dummyBoards.forEach { fakeBoardRepository.create(it) }
        }
    }

    @Test
    fun `find all boards returns deleted boards only`() = runTest {
        val boards = findDeletedBoards().first()
        assertThat(boards.all { it.board.deleted }).isTrue()
    }

    @Test
    fun `get boards use case returns list sorted by name ascending`(): Unit = runTest {
        val sortedBoards = findDeletedBoards(BoardOrder.A_Z).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `get boards use case returns list sorted by name descending`(): Unit = runTest {
        val sortedBoards = findDeletedBoards(BoardOrder.Z_A).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isGreaterThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @Test
    fun `get boards use case returns list sorted by creation date ascending`(): Unit = runTest {
        val sortedBoards = findDeletedBoards(BoardOrder.OLDEST).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isBefore(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }

    @Test
    fun `get boards use case returns list sorted by creation date descending`(): Unit = runTest {
        val sortedBoards = findDeletedBoards(BoardOrder.NEWEST).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isAfter(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }
}
