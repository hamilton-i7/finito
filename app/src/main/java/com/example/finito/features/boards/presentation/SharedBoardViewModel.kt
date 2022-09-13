package com.example.finito.features.boards.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.tasks.domain.entity.CompletedTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedBoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases
) : ViewModel() {

    fun onEvent(event: SharedBoardEvent) {
        when (event) {
            is SharedBoardEvent.ResetBoard -> resetBoard(event.board)
        }
    }

    private fun resetBoard(board: DetailedBoard) = viewModelScope.launch {
        board.let {
            boardUseCases.updateBoard(
                BoardWithLabelsAndTasks(
                    board = it.board,
                    labels = it.labels,
                    tasks = it.tasks.map { task -> CompletedTask(task.task.completed) }
                )
            )
        }
    }
}