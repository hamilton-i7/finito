package com.example.finito.core.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.util.SEARCH_DELAY_MILLIS
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferences: SharedPreferences,
) : ViewModel() {

    var showSearchbar by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var gridLayout by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.GRID_LAYOUT.name,
        true
    )); private set

    private var searchJob: Job? = null

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.ShowSearchBar -> onShowSearchBarChange(event.show)
            is AppEvent.SearchBoards -> onSearchBoards(event.query)
            AppEvent.ToggleLayout -> onToggleLayout()
        }
    }

    private fun onToggleLayout() {
        gridLayout = !gridLayout
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.GRID_LAYOUT.name, gridLayout)
            apply()
        }
    }

    private fun onSearchBoards(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            _eventFlow.emit(Event.SearchBoards(query))
        }
    }

    private fun onShowSearchBarChange(show: Boolean) = viewModelScope.launch {
        if (show == showSearchbar) return@launch
        if (!show) {
            searchQuery = ""
            _eventFlow.emit(Event.SearchBoards(query = ""))
        }
        showSearchbar = show
    }

    sealed class Event {
        data class SearchBoards(val query: String) : Event()
    }
}