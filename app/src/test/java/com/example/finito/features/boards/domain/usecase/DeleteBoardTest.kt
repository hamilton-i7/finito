package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
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
class DeleteBoardTest {
    private lateinit var deleteBoard: DeleteBoard
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        deleteBoard = DeleteBoard(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    boardId = index + 1,
                    name = "Board $c",
                )
            )
        }
        dummyBoards.forEach { fakeBoardRepository.create(it) }
    }

    @Test
    fun `delete board throws Exception if invalid ID`() {
        var board = dummyBoards.random().copy(boardId = 0)
        assertThrows(InvalidIdException::class.java) {
            runTest { deleteBoard(board) }
        }

        board = dummyBoards.random().copy(boardId = -2)
        assertThrows(InvalidIdException::class.java) {
            runTest { deleteBoard(board) }
        }
    }

    @Test
    fun `delete board does not remove any board`() = runTest {
        val latestId = fakeBoardRepository.findNewestId()
        val boardToDelete = dummyBoards.random().copy(boardId = latestId + 1)
        deleteBoard(boardToDelete)

        val boards = fakeBoardRepository.findAll().first()
        assertThat(boards.size).isEqualTo(dummyBoards.size)
    }

    @Test
    fun `delete board removes board from the list`() = runTest {
        val boardToDelete = dummyBoards.random()
        deleteBoard(boardToDelete)

        val boards = fakeBoardRepository.findAll().first()
        assertThat(boards.size).isLessThan(dummyBoards.size)
    }
}