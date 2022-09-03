package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DeleteBoardTest {
    private lateinit var deleteBoard: DeleteBoard
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        deleteBoard = DeleteBoard(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    boardId = index + 1,
                    name = "Board $c",
                )
            )
        }
        dummyBoards.forEach { fakeBoardRepository.create(it) }
    }

    @Test
    fun `Should throw InvalidIdException when ID is invalid`() {
        var board = dummyBoards.random().copy(boardId = 0)
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { deleteBoard(board) }
        }

        board = dummyBoards.random().copy(boardId = -2)
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { deleteBoard(board) }
        }
    }

    @Test
    fun `Should throw NotFoundException when board isn't found`() {
        val board = dummyBoards.random().copy(boardId = dummyBoards.map { it.boardId }.max() + 1)
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest { deleteBoard(board) }
        }
    }

    @Test
    fun `Should remove board from the list when it is found`() = runTest {
        val boardToDelete = dummyBoards.random()
        deleteBoard(boardToDelete)

        val boards = fakeBoardRepository.findAll().first()
        assertThat(boards.size).isLessThan(dummyBoards.size)
    }
}