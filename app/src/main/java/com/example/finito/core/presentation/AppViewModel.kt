package com.example.finito.core.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.util.SEARCH_DELAY_MILLIS
import com.example.finito.core.presentation.util.DialogType
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

    var showBottomBar by mutableStateOf(true)
        private set

    var showSearchbar by mutableStateOf(false)
        private set

    var showTopBarMenu by mutableStateOf(false)
        private set

    var dialogType by mutableStateOf<DialogType?>(null)
        private set

    var searchQuery by mutableStateOf(TextFieldValue())
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
            is AppEvent.ShowTopBarMenu -> showTopBarMenu = event.show
            is AppEvent.ShowDialog -> dialogType = event.type
            is AppEvent.ShowBottomBar -> showBottomBar = event.show
        }
    }

    private fun onToggleLayout() {
        gridLayout = !gridLayout
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.GRID_LAYOUT.name, gridLayout)
            apply()
        }
    }

    private fun onSearchBoards(query: TextFieldValue) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            _eventFlow.emit(Event.SearchBoards(query.text.trim()))
        }
    }

    private fun onShowSearchBarChange(show: Boolean) = viewModelScope.launch {
        if (show == showSearchbar) return@launch
        if (!show) {
            searchQuery = TextFieldValue()
            _eventFlow.emit(Event.SearchBoards(query = ""))
        }
        showSearchbar = show
    }

    sealed class Event {
        data class SearchBoards(val query: String) : Event()
    }
}