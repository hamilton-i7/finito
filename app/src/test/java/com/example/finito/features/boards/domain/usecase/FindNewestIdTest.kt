package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FindNewestIdTest {
    private lateinit var findNewestId: FindNewestId
    private lateinit var fakeBoardRepository: FakeBoardRepository
    private lateinit var dummyBoards: MutableList<Board>

    @Before
    fun setUp() = runTest {
        fakeBoardRepository = FakeBoardRepository()
        findNewestId = FindNewestId(fakeBoardRepository)
        dummyBoards = mutableListOf()

        ('A'..'Z').forEachIndexed { index, c ->
            dummyBoards.add(
                Board(
                    boardId = index,
                    name = if (index % 2 == 0) "Board $c" else "bÓäRd $c",
                    archived = index % 3 == 0,
                    deleted = index % 2 == 0
                )
            )
        }
        dummyBoards.forEach { fakeBoardRepository.create(it) }
    }

    @Test
    fun `find newest id returns last inserted board id`() = runTest {
        val lastBoardId = dummyBoards.map { it.boardId }.max()
        assertThat(findNewestId()).isEqualTo(lastBoardId)

        val newBoard = Board(
            boardId = dummyBoards.map { it.boardId }.max() + 1,
            name = "New Board"
        )
        fakeBoardRepository.create(newBoard)
        assertThat(findNewestId()).isEqualTo(newBoard.boardId)
    }
}