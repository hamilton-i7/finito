package com.example.finito.features.boards.presentation.screen.searchboard

import com.example.finito.features.labels.domain.entity.SimpleLabel

sealed class SearchBoardEvent {
    data class SelectLabel(val label: SimpleLabel) : SearchBoardEvent()

    data class LongSelectLabel(val label: SimpleLabel) : SearchBoardEvent()

    data class SearchBoards(val query: String) : SearchBoardEvent()

    object ConfirmSelectedLabels : SearchBoardEvent()

    data class ChangeMode(val mode: Mode) : SearchBoardEvent()

    enum class Mode {
        IDLE,
        SEARCH,
        SELECT
    }
}
