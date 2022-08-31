package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    fun setUp() {
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
        runBlocking {
            dummyBoards.forEach { fakeBoardRepository.create(it) }
        }
    }

    @Test
    fun `update board throws Exception if invalid ID`() {
        var board = dummyBoards.random().copy(boardId = 0)
        assertThrows(InvalidIdException::class.java) {
            runTest { updateBoard(board) }
        }

        board = dummyBoards.random().copy(boardId = -2)
        assertThrows(InvalidIdException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `update board throws Exception if invalid name`() {
        val emptyNameBoard = dummyBoards.random().copy(name = "")
        assertThrows(ResourceException::class.java) {
            runTest { updateBoard(emptyNameBoard) }
        }

        val blankNameBoard = dummyBoards.random().copy(name = "     ")
        assertThrows(ResourceException::class.java) {
            runTest { updateBoard(blankNameBoard) }
        }
    }

    @Test
    fun `update board throws Exception if board is archived and deleted`() {
        val board = dummyBoards.random().copy(
            name = "Invalid Board",
            archived = true,
            deleted = true
        )
        assertThrows(ResourceException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `update board makes changes to requested board`() = runTest {
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

    @Test
    fun `update board makes no changes`() = runTest {
        val board = dummyBoards.first { !it.archived && !it.deleted }
        assertThat(dummyBoards.find { it.boardId == board.boardId }?.name).startsWith("Board")
        val latestId = fakeBoardRepository.findNewestId()

        val updatedBoard = board.copy(
            boardId = latestId + 1,
            name = "Updated board",
            deleted = true,
            archived = false
        )
        updateBoard(updatedBoard)
        assertThat(fakeBoardRepository.findAll().first().any {
            it.board.name == "Updated board"
        }).isFalse()
    }
}