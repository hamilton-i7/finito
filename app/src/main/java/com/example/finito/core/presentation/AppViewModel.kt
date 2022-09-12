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

    var dynamicTopBarTitle by mutableStateOf("")
        private set

    private var searchJob: Job? = null

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun <S: Screen> onEvent(screen: S? = null, event: AppEvent<S>) {
        when (screen) {
            Screen.Home -> handleHomeScreenEvents(event as AppEvent.Home)
            Screen.Archive -> handleArchiveScreenEvents(event as AppEvent.Archive)
            null -> handleGenericEvents(event as AppEvent.Generic)
            else -> Unit
        }
    }

    private fun handleGenericEvents(event: AppEvent.Generic) {
        when (event) {
            is AppEvent.Generic.ShowBottomBar -> showBottomBar = event.show
            is AppEvent.Generic.ChangeDynamicTopBarTitle -> dynamicTopBarTitle = event.title
        }
    }

    private fun handleArchiveScreenEvents(event: AppEvent.Archive) {
        when (event) {
            is AppEvent.Archive.SearchBoards -> onSearchBoards(event.query, Screen.Archive)
            is AppEvent.Archive.ShowSearchBar -> onShowSearchBarChange(event.show, Screen.Archive)
            AppEvent.Archive.ToggleLayout -> onToggleLayout()
        }
    }

    private fun handleHomeScreenEvents(event: AppEvent.Home) {
        when (event) {
            is AppEvent.Home.ShowSearchBar -> onShowSearchBarChange(event.show, Screen.Home)
            is AppEvent.Home.SearchBoards -> onSearchBoards(event.query, Screen.Home)
            AppEvent.Home.ToggleLayout -> onToggleLayout()
        }
    }

    private fun onToggleLayout() {
        gridLayout = !gridLayout
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.GRID_LAYOUT.name, gridLayout)
            apply()
        }
    }

    private fun onSearchBoards(query: TextFieldValue, screen: Screen) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            when (screen) {
                Screen.Home -> {
                    _eventFlow.emit(Event.Home.SearchBoards(query.text.trim()))
                }
                Screen.Archive -> {
                    _eventFlow.emit(Event.Archive.SearchBoards(query.text.trim()))
                }
                else -> Unit
            }
        }
    }

    private fun onShowSearchBarChange(show: Boolean, screen: Screen) = viewModelScope.launch {
        if (show == showSearchbar) return@launch
        if (!show) {
            searchQuery = TextFieldValue()
            when (screen) {
                Screen.Home -> {
                    _eventFlow.emit(Event.Home.SearchBoards(query = ""))
                }
                Screen.Archive -> {
                    _eventFlow.emit(Event.Archive.SearchBoards(query = ""))
                }
                else -> Unit
            }
        }
        showSearchbar = show
    }

    sealed class Event {
        sealed class Home : Event() {
            data class SearchBoards(val query: String) : Home()
        }

        sealed class Archive : Event() {
            data class SearchBoards(val query: String) : Archive()
        }

        sealed class Board : Event() {
            object ArchiveBoard : Board()
        }
    }
}