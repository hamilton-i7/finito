package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.toSimpleLabel
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
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository
    private lateinit var dummyBoards: MutableList<BoardWithLabels>

    private val labels = listOf(
        Label(labelId = 1, name = "Label name"),
        Label(labelId = 2, name = "Label name"),
        Label(labelId = 3, name = "Label name"),
    )

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        updateBoard = UpdateBoard(fakeBoardRepository, fakeBoardLabelRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                BoardWithLabels(
                    board = Board(
                        boardId = index + 1,
                        name = "Board $c",
                        archived = index % 3 == 0,
                        deleted = index % 2 == 0
                    ),
                    labels = labels.take((0..labels.size).random()).map { it.toSimpleLabel() }
                )
            )
        }
        dummyBoards.forEach { fakeBoardRepository.create(it) }
    }

    @Test
    fun `Should throw NegativeIdException when ID is invalid`() {
        var board = dummyBoards.random().let {
            it.copy(
                board = it.board.copy(boardId = 0)
            )
        }
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { updateBoard(board) }
        }

        board = dummyBoards.random().let {
            it.copy(
                board = it.board.copy(boardId = -2)
            )
        }
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should throw EmptyException when name is empty`() {
        val emptyNameBoard = dummyBoards.random().let {
            it.copy(
                board = it.board.copy(name = "")
            )
        }
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { updateBoard(emptyNameBoard) }
        }

        val blankNameBoard = dummyBoards.random().let {
            it.copy(
                board = it.board.copy(name = "   ")
            )
        }
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest { updateBoard(blankNameBoard) }
        }
    }

    @Test
    fun `Should throw InvalidStateException when board state is invalid`() {
        val board = dummyBoards.random().let {
            it.copy(
                board = it.board.copy(
                    name = "Board name",
                    archived = true,
                    deleted = true
                )
            )
        }
        assertThrows(ResourceException.InvalidStateException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should throw NotFoundException when no board is found`() {
        val board = dummyBoards.first { !it.board.archived && !it.board.deleted }.let {
            it.copy(board = it.board.copy(boardId = 10_000))
        }
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should update board when it's found and its state is valid`() = runTest {
        val boardWithLabels = dummyBoards.random()
        assertThat(dummyBoards.find { it.board.boardId == boardWithLabels.board.boardId }?.board?.name).startsWith("Board")

        val updatedBoard = boardWithLabels.copy(
            board = boardWithLabels.board.copy(
                name = "Updated board",
                deleted = true,
                archived = false
            )
        )
        updateBoard(updatedBoard)
        assertThat(fakeBoardRepository.findOne(updatedBoard.board.boardId)?.board?.name)
            .isEqualTo("Updated board")
    }
}