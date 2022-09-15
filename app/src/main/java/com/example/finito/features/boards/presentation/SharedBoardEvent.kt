package com.example.finito.features.boards.presentation

import com.example.finito.features.boards.domain.entity.DetailedBoard

sealed class SharedBoardEvent {
    data class UndoBoardChange(val board: DetailedBoard) : SharedBoardEvent()

    object RefreshBoard : SharedBoardEvent()

    object ClearEvent : SharedBoardEvent()
}
