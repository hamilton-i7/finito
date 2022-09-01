package com.example.finito.features.boards.domain.usecase

import com.example.finito.core.util.InvalidIdException
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindOneBoardTest {
    private lateinit var findOneBoard: FindOneBoard
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        findOneBoard = FindOneBoard(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    boardId = index + 1,
                    name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                    archived = index % 3 == 0,
                    deleted = index % 2 == 0
                )
            )
        }
        dummyBoards.shuffle()
        dummyBoards.forEach { fakeBoardRepository.create(it) }
    }

    @Test
    fun `find one board throws Exception if invalid ID`() {
        assertThrows(InvalidIdException::class.java) {
            runTest { findOneBoard(-1) }
        }
        assertThrows(InvalidIdException::class.java) {
            runTest { findOneBoard(0) }
        }
        assertThrows(InvalidIdException::class.java) {
            runTest { findOneBoard(-23) }
        }
    }

    @Test
    fun `find one board returns null if no board is found`() = runTest {
        assertThat(findOneBoard(id = 10_000)).isNull()
    }

    @Test
    fun `find one board returns requested board`() = runTest {
        assertThat(findOneBoard(dummyBoards.random().boardId)).isNotNull()
    }
}