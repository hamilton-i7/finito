package com.example.finito.features.labels.presentation.screen.label

import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.data.repository.FakeBoardLabelRepository
import com.example.finito.features.boards.data.repository.FakeBoardRepository
import com.example.finito.features.boards.di.BoardModule
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.di.LabelModule
import com.example.finito.features.labels.domain.entity.Label
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LabelViewModelTest {

    private lateinit var labelViewModel: LabelViewModel

    @Mock
    lateinit var sharedPreferences: SharedPreferences

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
            set(Screen.LABEL_ROUTE_ARGUMENT, selectedLabelId)
        }

        boardRepository.findAll().take(15).map {
            BoardLabelCrossRef(
                boardId = it.boardId,
                labelId = selectedLabelId
            )
        }.let { boardLabelRepository.create(*it.toTypedArray()) }

        Dispatchers.setMain(Dispatchers.Unconfined)
        labelViewModel = LabelViewModel(
            boardUseCases = BoardModule.provideBoardUseCases(boardRepository, boardLabelRepository),
            labelUseCases = LabelModule.provideLabelUseCases(labelRepository),
            preferences = sharedPreferences,
            savedStateHandle = savedStateHandle
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should fetch label & boards when initialized`() {
        assertThat(labelViewModel.label).isNotNull()
        assertThat(labelViewModel.boards).isNotEmpty()
    }
}