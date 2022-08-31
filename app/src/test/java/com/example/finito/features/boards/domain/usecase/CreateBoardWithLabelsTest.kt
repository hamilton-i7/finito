package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.core.util.ResourceException
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateBoardWithLabelsTest {
    private lateinit var createBoardWithLabels: CreateBoardWithLabels
    private lateinit var fakeBoardRepository: FakeBoardRepository
    
    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        createBoardWithLabels = CreateBoardWithLabels(fakeBoardRepository)
    }
    
    @Test
    fun `create board with labels throws Exception if invalid name`() {
        val emptyNameBoard = Board(name = "")
        assertThrows(ResourceException::class.java) {
            runTest { createBoardWithLabels(emptyNameBoard, emptyList()) }
        }

        val blankNameBoard = Board(name = "     ")
        assertThrows(ResourceException::class.java) {
            runTest { createBoardWithLabels(blankNameBoard, emptyList()) }
        }
    }

    @Test
    fun `create board with labels throws Exception if invalid ID`() {
        val board = Board(name = "Board name")
        val labels = listOf(
            BoardLabelCrossRef(boardId = 0, labelId = 1),
            BoardLabelCrossRef(boardId = -2, labelId = -1),
            BoardLabelCrossRef(boardId = 1, labelId = 1),
            BoardLabelCrossRef(boardId = 1, labelId = 2),
        )
        assertThrows(InvalidIdException::class.java) {
            runTest { createBoardWithLabels(board, labels) }
        }
    }

    @Test
    fun `create board with labels throws Exception if board is archived and deleted`() {
        val board = Board(
            name = "Board name",
            archived = true,
            deleted = true
        )
        assertThrows(ResourceException::class.java) {
            runTest { createBoardWithLabels(board, emptyList()) }
        }
    }

    @Test
    fun `create board with labels inserts new board on the list`() = runTest {
        val board = Board(name = "Board name")
        val labels = listOf(
            BoardLabelCrossRef(boardId = 1, labelId = 1),
            BoardLabelCrossRef(boardId = 1, labelId = 2),
        )
        var boards = fakeBoardRepository.findAll().first()

        Truth.assertThat(boards.size).isEqualTo(0)
        fakeBoardRepository.create(board, labels)
        fakeBoardRepository.create(board, emptyList())

        boards = fakeBoardRepository.findAll().first()
        Truth.assertThat(boards.size).isEqualTo(2)
    }
}