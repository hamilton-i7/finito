package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateBoardTest {
    private lateinit var updateBoard: UpdateBoard
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        updateBoard = UpdateBoard(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    boardId = index + 1,
                    name = "Board $c",
                    archived = index % 3 == 0,
                    deleted = index % 2 == 0
                )
            )
        }
        dummyBoards.forEach { fakeBoardRepository.create(it) }
    }

    @Test
    fun `Should throw InvalidIdException when ID is invalid`() {
        var board = dummyBoards.random().copy(boardId = 0)
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { updateBoard(board) }
        }

        board = dummyBoards.random().copy(boardId = -2)
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should throw EmptyException when name is empty`() {
        val emptyNameBoard = dummyBoards.random().copy(name = "")
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { updateBoard(emptyNameBoard) }
        }

        val blankNameBoard = dummyBoards.random().copy(name = "     ")
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { updateBoard(blankNameBoard) }
        }
    }

    @Test
    fun `Should throw InvalidException when board state is invalid`() {
        val board = dummyBoards.random().copy(
            name = "Board name",
            archived = true,
            deleted = true
        )
        assertThrows(ResourceException.InvalidStateException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should throw NotFoundException when no board is found`() {
        val board = dummyBoards.first { !it.archived && !it.deleted }.copy(boardId = 10_000)
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should update board when it's found and its state is valid`() = runTest {
        val board = dummyBoards.random()
        assertThat(dummyBoards.find { it.boardId == board.boardId }?.name).startsWith("Board")

        val updatedBoard = board.copy(
            name = "Updated board",
            deleted = true,
            archived = false
        )
        updateBoard(updatedBoard)
        assertThat(fakeBoardRepository.findOne(updatedBoard.boardId)?.name)
            .isEqualTo("Updated board")
    }
}