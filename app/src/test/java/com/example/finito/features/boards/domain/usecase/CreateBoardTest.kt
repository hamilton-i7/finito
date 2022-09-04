package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.labels.domain.entity.Label
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
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository

    private val labels = listOf(
        Label(labelId = 1, name = "Label name"),
        Label(labelId = 2, name = "Label name"),
        Label(labelId = 3, name = "Label name"),
    )

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        createBoard = CreateBoard(fakeBoardRepository, fakeBoardLabelRepository)
    }

    @Test
    fun `Should throw EmptyException when board name is empty`() {
        val emptyNameBoard = BoardWithLabels(
            board = Board(name = "")
        )
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { createBoard(emptyNameBoard) }
        }

        val blankNameBoard = BoardWithLabels(
            board = Board(name = "     ")
        )
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { createBoard(blankNameBoard) }
        }
    }

    @Test
    fun `Should throw InvalidStateException when board state is invalid`() {
        val board = BoardWithLabels(
            board = Board(
                name = "Invalid Board",
                archived = true,
                deleted = true
            )
        )
        assertThrows(ResourceException::class.java) {
            runTest { createBoard(board) }
        }
    }

    @Test
    fun `Should insert new board into list board state is valid`() = runTest {
        val board = BoardWithLabels(
            board = Board(name = "Board name")
        )
        var boards = fakeBoardRepository.findAll().first()

        assertThat(boards.size).isEqualTo(0)
        createBoard(board)
        createBoard(board)

        boards = fakeBoardRepository.findAll().first()
        assertThat(boards.size).isEqualTo(2)
    }
}