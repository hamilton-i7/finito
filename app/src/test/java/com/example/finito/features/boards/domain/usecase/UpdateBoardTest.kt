package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
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

    @Before
    fun setUp() = runTest {
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        fakeLabelRepository = FakeLabelRepository()
        fakeBoardRepository = FakeBoardRepository(fakeLabelRepository, fakeBoardLabelRepository)
        updateBoard = UpdateBoard(fakeBoardRepository, fakeBoardLabelRepository)

        val dummyBoards = mutableListOf<Board>()
        val dummyLabels = listOf(
            Label(name = "Label name"),
            Label(name = "Label name"),
            Label(name = "Label name"),
        )

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                    archived = index % 5 == 0,
                    deleted = index % 7 == 0,
                    createdAt = LocalDateTime.now().plusMinutes(index.toLong())
                )
            )
        }
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
        dummyLabels.forEach { fakeLabelRepository.create(it) }

        val labelIds = fakeLabelRepository.findSimpleLabels().first().map { it.labelId }
        val boardIds = fakeBoardRepository.findAll().map { it.boardId }

        boardIds.filter { it % 2 == 0 }.map {
            BoardLabelCrossRef(
                boardId = it,
                labelId = labelIds.random()
            )
        }.let { fakeBoardLabelRepository.create(*it.toTypedArray()) }
    }

    @Test
    fun `Should throw NegativeIdException when ID is invalid`() {
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest {
                val board = BoardWithLabelsAndTasks(
                    board = fakeBoardRepository.findActiveBoards().first().random().board.copy(boardId = 0)
                )
                updateBoard(board)
            }
        }
        assertThrows(ResourceException.NegativeIdException::class.java) {
            runTest {
                val board = BoardWithLabelsAndTasks(
                    board = fakeBoardRepository.findActiveBoards().first().random().board.copy(boardId = -2)
                )
                updateBoard(board)
            }
        }
    }

    @Test
    fun `Should throw EmptyException when name is empty`() {
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest {
                val board = BoardWithLabelsAndTasks(
                    board = fakeBoardRepository.findActiveBoards().first().random().board.copy(name = "")
                )
                updateBoard(board)
            }
        }
        assertThrows(ResourceException.EmptyException::class.java) {
            runTest {
                val board = BoardWithLabelsAndTasks(
                    board = fakeBoardRepository.findActiveBoards().first().random().board.copy(name = "   ")
                )
                updateBoard(board)
            }
        }
    }

    @Test
    fun `Should throw InvalidStateException when board state is invalid`() {
        assertThrows(ResourceException.InvalidStateException::class.java) {
            runTest {
                val board = BoardWithLabelsAndTasks(
                    board = fakeBoardRepository.findActiveBoards().first().random().board.copy(
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
        assertThrows(ResourceException.NotFoundException::class.java) {
            runTest {
                val board = BoardWithLabelsAndTasks(
                    board = fakeBoardRepository.findActiveBoards().first().random().board.copy(
                        boardId = 10_000
                    )
                )
                updateBoard(board)
            }
        }
    }

    @Test
    fun `Should update board when it's found and its state is valid`() = runTest {
        val board = fakeBoardRepository.findActiveBoards().first().random().board
        assertThat(
            fakeBoardRepository.findOne(board.boardId)?.board?.normalizedName
        ).startsWith("board")

        updateBoard(
            BoardWithLabelsAndTasks(board = board.copy(name = "Updated board"))
        )
        assertThat(fakeBoardRepository.findOne(board.boardId)?.board?.name)
            .isEqualTo("Updated board")
    }

    @Test
    fun `Should delete board-label refs when labels changed`() = runTest {
        val boardWithLabels = fakeBoardRepository.findActiveBoards().first().first { it.labels.isNotEmpty() }
        val labelId = boardWithLabels.labels.random().labelId

        assertThat(
            fakeBoardLabelRepository.findAllByBoardId(
                boardWithLabels.board.boardId
            ).find { it.labelId == labelId }
        ).isNotNull()

        updateBoard(boardWithLabels.copy(labels = emptyList()))
        assertThat(
            fakeBoardLabelRepository.findAllByBoardId(
                boardWithLabels.board.boardId
            ).find { it.labelId == labelId }
        ).isNull()
    }

    @Test
    fun `Should create board-label refs when new labels were added`() = runTest {
        val boardWithLabels = fakeBoardRepository.findActiveBoards().first().first { it.labels.isNotEmpty() }
        val labelIds = boardWithLabels.labels.groupBy { it.labelId }
        val oldRefsSize = fakeBoardLabelRepository.findAllByBoardId(
            boardWithLabels.board.boardId
        ).size

        fakeLabelRepository.findSimpleLabels().first().filter {
            labelIds[it.labelId] == null
        }.let {
            updateBoard(
                boardWithLabels.copy(labels = boardWithLabels.labels + it)
            )
        }

        val newRefsSize = fakeBoardLabelRepository.findAllByBoardId(
            boardWithLabels.board.boardId
        ).size

        assertThat(newRefsSize).isGreaterThan(oldRefsSize)
    }
}