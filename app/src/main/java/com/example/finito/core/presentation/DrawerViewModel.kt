package com.example.finito.core.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.di.PreferencesModule
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val preferences: SharedPreferences
) : ViewModel() {

    var currentRoute by mutableStateOf(Screen.Home.route)
        private set

    var boards by mutableStateOf<List<SimpleBoard>>(emptyList())
        private set

    var boardsExpanded by mutableStateOf(
        preferences.getBoolean(PreferencesModule.TAG.EXPAND_BOARDS.name, true)
    ); private set

    var labels by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var labelsExpanded by mutableStateOf(
        preferences.getBoolean(PreferencesModule.TAG.EXPAND_LABELS.name, true)
    ); private set

    init {
        fetchLabels()
        fetchBoards()
    }

    fun onEvent(event: DrawerEvent) {
        when (event) {
            is DrawerEvent.ChangeRoute -> changeRoute(event.route)
            DrawerEvent.ToggleBoardsExpanded -> toggleBoardsExpanded()
            DrawerEvent.ToggleLabelsExpanded -> toggleLabelsExpanded()
        }
    }

    private fun fetchLabels() = viewModelScope.launch {
        labelUseCases.findSimpleLabels().data.onEach { labels ->
            this@DrawerViewModel.labels = labels
        }.launchIn(viewModelScope)
    }

    private fun fetchBoards() = viewModelScope.launch {
        boardUseCases.findSimpleBoards().data.onEach { boards ->
            this@DrawerViewModel.boards = boards
        }.launchIn(viewModelScope)
    }

    private fun changeRoute(route: String) {
        currentRoute = route
    }

    private fun toggleBoardsExpanded() {
        boardsExpanded = !boardsExpanded
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.EXPAND_BOARDS.name, boardsExpanded)
            apply()
        }
    }

    private fun toggleLabelsExpanded() {
        labelsExpanded = !labelsExpanded
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.EXPAND_LABELS.name, labelsExpanded)
            apply()
        }
    }
}