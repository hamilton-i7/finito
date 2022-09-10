package com.example.finito.features.boards.presentation.addeditboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.util.TextFieldState
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditBoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var board by mutableStateOf<Board?>(null)
        private set

    var labels by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var selectedLabels by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var nameState by mutableStateOf(TextFieldState())
        private set

    var showLabels by mutableStateOf(true)
        private set

    init {
        fetchBoard()
        fetchLabels()
    }

    fun onEvent(event: AddEditBoardEvent) {
        when (event) {
            is AddEditBoardEvent.AddLabel -> TODO()
            is AddEditBoardEvent.ChangeName -> TODO()
            AddEditBoardEvent.CreateBoard -> TODO()
            AddEditBoardEvent.DeleteBoard -> TODO()
            AddEditBoardEvent.EditBoard -> TODO()
            is AddEditBoardEvent.RemoveLabel -> TODO()
            AddEditBoardEvent.ToggleLabelsVisibility -> TODO()
        }
    }

    private fun fetchBoard() {
        savedStateHandle.get<Int>(Screen.BOARD_ROUTE_ARGUMENT)?.let { boardId ->
            viewModelScope.launch {
                boardUseCases.findOneBoard(boardId).onEach {
                    board = it.board
                }.launchIn(viewModelScope)
            }
        }
    }

    private fun fetchLabels() = viewModelScope.launch {
        labelUseCases.findSimpleLabels().onEach {
            labels = it
        }.launchIn(viewModelScope)
    }
}