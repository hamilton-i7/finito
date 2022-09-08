package com.example.finito.features.boards.presentation.board

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.utils.DeactivateMode
import com.example.finito.features.subtasks.domain.usecase.SubtaskUseCases
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val taskUseCases: TaskUseCases,
    private val subtaskUseCases: SubtaskUseCases,
    private val preferences: SharedPreferences,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var board by mutableStateOf<DetailedBoard?>(null)
        private set

    var showCompletedTasks by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name,
        true
    )); private set

    var showScreenMenu by mutableStateOf(false)
        private set

    var showDialog by mutableStateOf(false)
        private set

    var dialogType by mutableStateOf<BoardEvent.DialogType>(
        BoardEvent.DialogType.DeleteCompletedTasks
    ); private set

    init {
        fetchBoard()
    }

    fun onEvent(event: BoardEvent) {
        when (event) {
            is BoardEvent.ArchiveBoard -> deactivateBoard(event.board, DeactivateMode.ARCHIVE)
            is BoardEvent.DeleteBoard -> deactivateBoard(event.board, DeactivateMode.DELETE)
            is BoardEvent.ChangeTaskDateTime -> TODO()
            is BoardEvent.ChangeTaskPriority -> TODO()
            is BoardEvent.CheckTask -> TODO()
            BoardEvent.DeleteCompletedTas -> TODO()
            is BoardEvent.ShowDateTimeDialog -> TODO()
            is BoardEvent.ShowPriorityDialog -> TODO()
            is BoardEvent.ShowScreenMenu -> TODO()
            BoardEvent.ToggleCompletedTasksVisibility -> onShowCompletedTasksChange()
            is BoardEvent.UncheckTask -> TODO()
        }
    }

    private fun onShowCompletedTasksChange() {
        showCompletedTasks = !showCompletedTasks
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name, showCompletedTasks)
            apply()
        }
    }

    private fun fetchBoard() {
        savedStateHandle.get<Int>(Screen.BOARD_ROUTE_ARGUMENT)?.let { boardId ->
            viewModelScope.launch {
                board = boardUseCases.findOneBoard(boardId)
            }
        }
    }

    private fun deactivateBoard(
        board: BoardWithLabelsAndTasks,
        mode: DeactivateMode,
    ) = viewModelScope.launch {
        try {
            with(board) {
                boardUseCases.updateBoard(
                    when (mode) {
                        DeactivateMode.ARCHIVE -> copy(board = this.board.copy(archived = true))
                        DeactivateMode.DELETE -> copy(board = this.board.copy(
                            deleted = true,
                            removedAt = LocalDateTime.now()
                        ))
                    }
                )
            }
        } catch (e: ResourceException.NegativeIdException) {

        } catch (e: ResourceException.EmptyException) {

        } catch (e: ResourceException.InvalidStateException) {

        } catch (e: ResourceException.NotFoundException) {}
    }
}