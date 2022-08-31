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

class FindAllBoardsTest {

    private lateinit var findAllBoards: FindAllBoards
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        findAllBoards = FindAllBoards(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            runBlocking {
                delay(25L)
                dummyBoards.add(
                    Board(
                        boardId = index,
                        name = if (index % 2 == 0) "Board $c" else "bÓäRd $c"
                    )
                )
            }
        }
        dummyBoards.shuffle()
        runBlocking {
            dummyBoards.forEach { fakeBoardRepository.create(it) }
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get boards use case returns list sorted by name ascending`(): Unit = runTest {
        val sortedBoards = findAllBoards(BoardOrder.A_Z).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get boards use case returns list sorted by name descending`(): Unit = runTest {
        val sortedBoards = findAllBoards(BoardOrder.Z_A).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isGreaterThan(sortedBoards[i+1].board.normalizedName)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get boards use case returns list sorted by creation date ascending`(): Unit = runTest {
        val sortedBoards = findAllBoards(BoardOrder.OLDEST).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isBefore(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get boards use case returns list sorted by creation date descending`(): Unit = runTest {
        val sortedBoards = findAllBoards(BoardOrder.NEWEST).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(
                sortedBoards[i].board.createdAt.isAfter(sortedBoards[i+1].board.createdAt)
            ).isTrue()
        }
    }
}
