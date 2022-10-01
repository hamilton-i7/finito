package com.example.finito.features.boards.domain.usecase

import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.Label
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
    private lateinit var fakeBoardLabelRepository: FakeBoardLabelRepository
    private lateinit var fakeLabelRepository: FakeLabelRepository

    private lateinit var dummyBoards: MutableList<Board>
    private lateinit var dummyLabels: List<Label>

    @Before
    fun setUp() = runTest {
        fakeBoardLabelRepository = FakeBoardLabelRepository()
        fakeLabelRepository = FakeLabelRepository()
        fakeBoardRepository = FakeBoardRepository(fakeLabelRepository, fakeBoardLabelRepository)
        findSimpleBoards = FindSimpleBoards(fakeBoardRepository)
        dummyBoards = mutableListOf()
        dummyLabels = listOf(
            Label(name = "Label name"),
            Label(name = "Label name"),
            Label(name = "Label name"),
        )

        dummyBoards.addAll(Board.dummyBoards)
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
    fun `Should return active boards when asked`() = runTest {
        val boardIds = findSimpleBoards().first().map { it.boardId }
        val deletedBoards = dummyBoards.filter { it.state == BoardState.DELETED }.groupBy { it.boardId }
        boardIds.forEach {
            assertThat(deletedBoards[it]).isNull()
        }

        val archivedBoards = dummyBoards.filter { it.state == BoardState.ARCHIVED }.groupBy { it.boardId }
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
