package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.util.BoardOrder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetBoardsUseCaseTest {

    private lateinit var getBoardsUseCase: GetBoardsUseCase
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        getBoardsUseCase = GetBoardsUseCase(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    boardId = index,
                    name = "Board $c"
                )
            )
        }
        dummyBoards.shuffle()
        runBlocking {
            dummyBoards.forEach { fakeBoardRepository.addBoard(it) }
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get boards use case returns list sorted by name ascending`(): Unit = runTest {
        val sortedBoards = getBoardsUseCase(BoardOrder.A_Z).first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].board.normalizedName)
                .isLessThan(sortedBoards[i+1].board.normalizedName)
        }
    }
}