package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateBoardLabelTest {
    private lateinit var createBoardLabel: CreateBoardLabel
    private lateinit var fakeBoardRepository: FakeBoardRepository

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        createBoardLabel = CreateBoardLabel(fakeBoardRepository)
    }

    @Test
    fun `create board label throws Exception if invalid ID`() {
        val boardLabels = arrayOf(
            BoardLabelCrossRef(boardId = 1, labelId = 2),
            BoardLabelCrossRef(boardId = 1, labelId = 3),
            BoardLabelCrossRef(boardId = 2, labelId = 3),
            BoardLabelCrossRef(boardId = 2, labelId = 0),
            BoardLabelCrossRef(boardId = -1, labelId = 0),
        )
        assertThrows(InvalidIdException::class.java) {
            runTest { createBoardLabel(*boardLabels) }
        }
    }

    @Test
    fun `create board label inserts board label into list`() = runTest {
        assertThat(fakeBoardRepository.boardLabels.size).isEqualTo(0)

        val boardLabels = arrayOf(
            BoardLabelCrossRef(boardId = 1, labelId = 2),
            BoardLabelCrossRef(boardId = 1, labelId = 3),
            BoardLabelCrossRef(boardId = 2, labelId = 3),
        )
        createBoardLabel(*boardLabels)
        assertThat(fakeBoardRepository.boardLabels.size).isEqualTo(3)
    }
}