package com.example.finito.features.boards.presentation

import com.example.finito.features.boards.domain.entity.DetailedBoard

sealed class SharedBoardEvent {
    data class ResetBoard(val board: DetailedBoard) : SharedBoardEvent()
}