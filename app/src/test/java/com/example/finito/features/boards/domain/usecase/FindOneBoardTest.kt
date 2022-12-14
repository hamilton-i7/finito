package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.domain.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindOneBoardTest {
    private lateinit var findOneBoard: FindOneBoard

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
        findOneBoard = FindOneBoard(fakeBoardRepository)
        dummyBoards = mutableListOf()
        dummyLabels = listOf(
            Label(name = "Label name"),
            Label(name = "Label name"),
            Label(name = "Label name"),
        )

        dummyBoards.addAll(Board.dummyBoards)
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
        val boardId = fakeBoardRepository.findActiveBoards().first().random().board.boardId
        assertThat(findOneBoard(boardId)).isNotNull()
    }
}