package com.example.finito.core.presentation

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
class AppViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases
) : ViewModel() {

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.UndoBoardChange -> onUndoBoardChange(event.board)
        }
    }

    private fun onUndoBoardChange(originalBoard: DetailedBoard) = viewModelScope.launch {
        originalBoard.let {
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