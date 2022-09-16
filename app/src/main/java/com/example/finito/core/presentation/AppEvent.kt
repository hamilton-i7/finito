package com.example.finito.core.presentation

import com.example.finito.features.boards.domain.entity.DetailedBoard

sealed class AppEvent {
    data class UndoBoardChange(val board: DetailedBoard) : AppEvent()
}
