package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindOneBoardTest {
    private lateinit var findOneBoard: FindOneBoard
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<BoardWithLabels>

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        findOneBoard = FindOneBoard(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                BoardWithLabels(
                    board = Board(
                        boardId = index + 1,
                        name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                        archived = index % 3 == 0,
                        deleted = index % 2 == 0
                    )
                )
            )
        }
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
    }

    @Test
    fun `Should throw NegativeIdException when ID is invalid`() {
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { findOneBoard(-1) }
        }
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { findOneBoard(0) }
        }
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { findOneBoard(-23) }
        }
    }

    @Test
    fun `Should throw NotFoundException when no board is found`() {
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest { findOneBoard(10_000) }
        }
    }

    @Test
    fun `Should return board when it is found`() = runTest {
        assertThat(findOneBoard(dummyBoards.random().board.boardId)).isNotNull()
    }
}