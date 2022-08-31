package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.ResourceException
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
class AddBoardUseCaseTest {
    private lateinit var addBoardUseCase: AddBoardUseCase
    private lateinit var fakeBoardRepository: FakeBoardRepository

    @Before
    fun setUp() {
        fakeBoardRepository = FakeBoardRepository()
        addBoardUseCase = AddBoardUseCase(fakeBoardRepository)
    }

    @Test
    fun `add board throws Exception if invalid name`() {
        val emptyNameBoard = Board(name = "")
        assertThrows(ResourceException::class.java) {
            runTest { addBoardUseCase(emptyNameBoard) }
        }

        val blankNameBoard = Board(name = "     ")
        assertThrows(ResourceException::class.java) {
            runTest { addBoardUseCase(blankNameBoard) }
        }
    }

    @Test
    fun `add board inserts new board on list`() = runTest {
        val board = Board(name = "Board name")
        var boards = fakeBoardRepository.findAll().first()

        assertThat(boards.size).isEqualTo(0)
        addBoardUseCase(board)
        addBoardUseCase(board)

        boards = fakeBoardRepository.findAll().first()
        assertThat(boards.size).isEqualTo(2)
    }
}