package com.example.finito.core.presentation

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.finito.core.di.PreferencesModule
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val preferences: SharedPreferences
) : ViewModel() {
    var expandBoards by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.EXPAND_BOARDS.name,
        false
    )); private set
    var expandLabels by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.EXPAND_LABELS.name,
        false
    )); private set

    fun onExpandBoardsChange() {
        expandBoards = !expandBoards
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.EXPAND_BOARDS.name, expandBoards)
            apply()
        }
    }

    fun onExpandLabelsChange() {
        expandLabels = !expandLabels
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.EXPAND_LABELS.name, expandLabels)
            apply()
        }
    }
}