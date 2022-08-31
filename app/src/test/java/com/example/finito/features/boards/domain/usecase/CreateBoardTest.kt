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
class CreateBoardTest {
    private lateinit var createBoard: CreateBoard
    private lateinit var fakeBoardRepository: FakeBoardRepository

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        createBoard = CreateBoard(fakeBoardRepository)
    }

    @Test
    fun `add board throws Exception if invalid name`() {
        val emptyNameBoard = Board(name = "")
        assertThrows(ResourceException::class.java) {
            runTest { createBoard(emptyNameBoard) }
        }

        val blankNameBoard = Board(name = "     ")
        assertThrows(ResourceException::class.java) {
            runTest { createBoard(blankNameBoard) }
        }
    }

    @Test
    fun `create board throws Exception if board is archived and deleted`() {
        val board = Board(
            name = "Invalid Board",
            archived = true,
            deleted = true
        )
        assertThrows(ResourceException::class.java) {
            runTest { createBoard(board) }
        }
    }

    @Test
    fun `add board inserts new board on list`() = runTest {
        val board = Board(name = "Board name")
        var boards = fakeBoardRepository.findAll().first()

        assertThat(boards.size).isEqualTo(0)
        createBoard(board)
        createBoard(board)

        boards = fakeBoardRepository.findAll().first()
        assertThat(boards.size).isEqualTo(2)
    }
}