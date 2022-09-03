package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindSimpleBoardsTest {

    private lateinit var findSimpleBoards: FindSimpleBoards
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        findSimpleBoards = FindSimpleBoards(fakeBoardRepository)
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
    fun `Should return active boards when asked`() = runTest {
        val boardIds = findSimpleBoards().first().onEach(::println).map { it.boardId }
        val deletedBoards = dummyBoards.filter { it.deleted }.groupBy { it.boardId }
        boardIds.forEach {
            assertThat(deletedBoards[it]).isNull()
        }

        val archivedBoards = dummyBoards.filter { it.archived }.groupBy { it.boardId }
        boardIds.forEach {
            assertThat(archivedBoards[it]).isNull()
        }
    }

    @Test
    fun `Should return boards sorted by name ascending when asked`() = runTest {
        val sortedBoards = findSimpleBoards().first()

        for (i in 0..sortedBoards.size - 2) {
            assertThat(sortedBoards[i].normalizedName)
                .isLessThan(sortedBoards[i+1].normalizedName)
        }
    }
}
