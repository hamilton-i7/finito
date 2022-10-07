package com.example.finito.features.labels.presentation.screen.label

import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.di.BoardModule
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.di.LabelModule
import com.example.finito.features.labels.domain.entity.Label
import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class LabelViewModelTest {

    private lateinit var labelViewModel: LabelViewModel

    private lateinit var boardUseCases: BoardUseCases

    private lateinit var sharedPreferences: SharedPreferences

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {
        val labelRepository = FakeLabelRepository()
        val boardLabelRepository = FakeBoardLabelRepository()
        val boardRepository = FakeBoardRepository(labelRepository, boardLabelRepository)

        Label.dummyLabels.forEach { labelRepository.create(it) }
        Board.dummyBoards.forEach { boardRepository.create(it) }

        val labelIds = labelRepository.findAll().map { it.labelId }
        val selectedLabelId = labelIds.random()
        val savedStateHandle = SavedStateHandle().apply {
            set(Screen.LABEL_ID_ARGUMENT, selectedLabelId)
        }

        boardRepository.findAll().take(15).map {
            BoardLabelCrossRef(
                boardId = it.boardId,
                labelId = selectedLabelId
            )
        }.let { boardLabelRepository.create(*it.toTypedArray()) }

        boardUseCases = BoardModule.provideBoardUseCases(boardRepository, boardLabelRepository)
        sharedPreferences = SPMockBuilder().createSharedPreferences()

        Dispatchers.setMain(testDispatcher)
        labelViewModel = LabelViewModel(
            boardUseCases = boardUseCases,
            labelUseCases = LabelModule.provideLabelUseCases(labelRepository),
            preferences = sharedPreferences,
            savedStateHandle = savedStateHandle
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun teardown() {
        Dispatchers.resetMain()
        testDispatcher.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should fetch label & boards when initialized`() = runTest {
        advanceUntilIdle()
        assertThat(labelViewModel.label).isNotNull()
        assertThat(labelViewModel.boards).isNotEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should emit ShowSnackbar event when board archived`() = runTest {
        val board = boardUseCases.findActiveBoards().data.first().random()
        labelViewModel.onEvent(LabelEvent.ArchiveBoard(board))
        val event = labelViewModel.eventFlow.first()
        assertThat(event).isInstanceOf(LabelViewModel.Event.Snackbar::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should emit ShowSnackbar event when board deleted`() = runTest {
        val board = boardUseCases.findActiveBoards().data.first().random()
        labelViewModel.onEvent(LabelEvent.MoveBoardToTrash(board))
        val event = labelViewModel.eventFlow.first()
        assertThat(event).isInstanceOf(LabelViewModel.Event.Snackbar::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should emit NavigateHome event when label deleted`() = runTest {
        labelViewModel.onEvent(LabelEvent.DeleteLabel)
        val event = labelViewModel.eventFlow.first()
        assertThat(event).isEqualTo(LabelViewModel.Event.NavigateHome)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should set showCardMenu to true`() = runTest {
        val boardId = boardUseCases.findActiveBoards().data.first().random().board.boardId
        labelViewModel.onEvent(LabelEvent.ShowCardMenu(boardId, show = true))

        assertThat(labelViewModel.showCardMenu).isTrue()
    }

    @Test
    fun `should set showDialog to requested dialog type`() {
        labelViewModel.onEvent(LabelEvent.ShowDialog(type = LabelEvent.DialogType.Rename))
        assertThat(labelViewModel.dialogType).isEqualTo(LabelEvent.DialogType.Rename)
    }

    @Test
    fun `should set showSearchBar to true`() {
        labelViewModel.onEvent(LabelEvent.ShowSearchBar(show = true))
        assertThat(labelViewModel.showSearchBar).isTrue()
    }

    @Test
    fun `should set showScreenMenu to true`() {
        labelViewModel.onEvent(LabelEvent.ShowScreenMenu(show = true))
        assertThat(labelViewModel.showScreenMenu).isTrue()
    }

    @Test
    fun `should toggle gridLayout`() {
        labelViewModel.onEvent(LabelEvent.ToggleLayout)
        assertThat(labelViewModel.gridLayout).isFalse()

        labelViewModel.onEvent(LabelEvent.ToggleLayout)
        assertThat(labelViewModel.gridLayout).isTrue()
    }
}