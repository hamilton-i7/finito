package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class UpdateBoardTest {
    private lateinit var updateBoard: UpdateBoard

    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository
    private lateinit var fakeLabelRepository: FakeLabelRepository

    private lateinit var dummyBoards: MutableList<Board>
    private lateinit var dummyLabels: List<Label>

    @Before
    fun setUp() = runTest {
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        fakeLabelRepository = FakeLabelRepository()
        fakeBoardRepository = FakeBoardRepository(fakeLabelRepository, fakeBoardLabelRepository)
        updateBoard = UpdateBoard(fakeBoardRepository, fakeBoardLabelRepository)
        dummyBoards = mutableListOf()
        dummyLabels = listOf(
            Label(name = "Label name"),
            Label(name = "Label name"),
            Label(name = "Label name"),
        )

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                    archived = index % 3 == 0,
                    deleted = index % 2 == 0,
                    createdAt = LocalDateTime.now().plusMinutes(index.toLong())
                )
            )
        }
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
        dummyLabels.forEach { fakeLabelRepository.create(it) }
        dummyBoards.filter { it.boardId % 3 == 0 }.map {
            BoardLabelCrossRef(
                boardId = it.boardId,
                labelId = dummyLabels.random().labelId
            )
        }.let { fakeBoardLabelRepository.create(*it.toTypedArray()) }
    }

    @Test
    fun `Should throw NegativeIdException when ID is invalid`() {
        var board = BoardWithLabels(board = dummyBoards.random().copy(boardId = 0))
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { updateBoard(board) }
        }

        board = BoardWithLabels(board = dummyBoards.random().copy(boardId = -2))
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should throw EmptyException when name is empty`() {
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest {
                val board = BoardWithLabels(
                    board = fakeBoardRepository.findAll().first().random().board.copy(name = "")
                )
                updateBoard(board)
            }
        }
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest {
                val board = BoardWithLabels(
                    board = fakeBoardRepository.findAll().first().random().board.copy(name = "   ")
                )
                updateBoard(board)
            }
        }
    }

    @Test
    fun `Should throw InvalidStateException when board state is invalid`() {
        assertThrows(ResourceException.InvalidStateException::class.java) {
            runTest {
                val board = BoardWithLabels(
                    board = fakeBoardRepository.findAll().first().random().board.copy(
                        archived = true,
                        deleted = true
                    )
                )
                updateBoard(board)
            }
        }
    }

    @Test
    fun `Should throw NotFoundException when no board is found`() {
        val board = BoardWithLabels(board = dummyBoards.random().copy(boardId = 10_000))
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest { updateBoard(board) }
        }
    }

    @Test
    fun `Should update board when it's found and its state is valid`() = runTest {
        val board = fakeBoardRepository.findAll().first().random().board
        assertThat(
            fakeBoardRepository.findOne(board.boardId)?.board?.normalizedName
        ).startsWith("board")

        updateBoard(
            BoardWithLabels(board = board.copy(name = "Updated board"))
        )
        assertThat(fakeBoardRepository.findOne(board.boardId)?.board?.name)
            .isEqualTo("Updated board")
    }
}