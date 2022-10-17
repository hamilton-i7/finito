package com.example.finito.features.boards.presentation.screen.searchboard

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.domain.util.SEARCH_DELAY_MILLIS
import com.example.finito.core.presentation.util.PreferencesKeys
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchBoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val preferences: SharedPreferences,
) : ViewModel() {

    var boards by mutableStateOf<List<BoardWithLabelsAndTasks>>(emptyList())
        private set

    var labels by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var labelFilters by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var searchQueryState by mutableStateOf(TextFieldState.Default)
        private set

    private var fetchBoardsJob: Job? = null

    private var searchJob: Job? = null

    var gridLayout by mutableStateOf(preferences.getBoolean(
        PreferencesKeys.GRID_LAYOUT,
        true
    )); private set

    var mode by mutableStateOf(SearchBoardEvent.Mode.IDLE)
        private set

    init {
        fetchLabels()
    }

    fun onEvent(event: SearchBoardEvent) {
        when (event) {
            SearchBoardEvent.ConfirmSelectedLabels -> onConfirmSelectedLabels()
            is SearchBoardEvent.SearchBoards -> onSearchBoards(event.query)
            is SearchBoardEvent.SelectLabel -> onSelectLabel(event.label)
            is SearchBoardEvent.LongSelectLabel -> onLongSelectLabel(event.label)
            is SearchBoardEvent.ChangeMode -> onChangeMode(event.mode)
        }
    }

    private fun onChangeMode(mode: SearchBoardEvent.Mode) {
        this.mode = mode
    }

    private fun onConfirmSelectedLabels() {
        mode = SearchBoardEvent.Mode.SEARCH
    }

    private fun onLongSelectLabel(label: SimpleLabel) {
        labelFilters = labelFilters + listOf(label)
        mode = SearchBoardEvent.Mode.SELECT
    }

    private fun onSelectLabel(label: SimpleLabel) {
        val exists = labelFilters.contains(label)
        labelFilters = if (exists) {
            labelFilters.filter { it != label }
        } else {
            labelFilters + listOf(label)
        }

        if (labelFilters.isEmpty()) {
            mode = SearchBoardEvent.Mode.IDLE
        }
    }

    private fun onSearchBoards(query: String) {
        searchQueryState = searchQueryState.copy(value = query)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            fetchBoards()
        }
    }

    private fun fetchLabels() = viewModelScope.launch {
        labelUseCases.findSimpleLabels().data.onEach { labels ->
            this@SearchBoardViewModel.labels = labels
        }.launchIn(viewModelScope)
    }

    private fun fetchBoards() = viewModelScope.launch {
        fetchBoardsJob?.cancel()
        fetchBoardsJob = boardUseCases.findActiveBoards(
            searchQuery = searchQueryState.value,
            labelIds = labelFilters.map { it.labelId }.toIntArray()
        ).data.onEach { boards ->
            this@SearchBoardViewModel.boards = boards
        }.launchIn(viewModelScope)
    }
}