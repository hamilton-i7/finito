package com.example.finito.features.boards.presentation.screen.board

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.Priority
import com.example.finito.core.domain.Result
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.utils.DeactivateMode
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.usecase.SubtaskUseCases
import com.example.finito.features.tasks.domain.entity.*
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val taskUseCases: TaskUseCases,
    private val subtaskUseCases: SubtaskUseCases,
    private val preferences: SharedPreferences,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var boardState by mutableStateOf(BoardState.ACTIVE)
        private set

    var board by mutableStateOf<DetailedBoard?>(null)
        private set

    var showCompletedTasks by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name,
        true
    )); private set

    var showScreenMenu by mutableStateOf(false)
        private set

    var dialogType by mutableStateOf<BoardEvent.DialogType?>(null)
        private set

    var tasks by mutableStateOf<List<TaskWithSubtasks>>(emptyList())
        private set

    var selectedTask by mutableStateOf<Task?>(null)
        private set

    var newTaskNameState by mutableStateOf(TextFieldState())
        private set

    var selectedPriority by mutableStateOf<Priority?>(null)
        private set

    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var selectedTime by mutableStateOf<LocalTime?>(null)
        private set

    var draggingContent by mutableStateOf<BoardEvent.DraggingContent?>(null)
        private set

    private var recentlyReorderedSubtasks: List<Subtask> = emptyList()
    private var relatedTaskId: Int? = null

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchBoard()
        fetchBoardState()
    }

    fun onEvent(event: BoardEvent) {
        when (event) {
            BoardEvent.ArchiveBoard -> onDeactivateBoard(DeactivateMode.ARCHIVE)
            BoardEvent.DeleteBoard -> onDeactivateBoard(DeactivateMode.DELETE)
            BoardEvent.RestoreBoard -> restoreBoard()
            is BoardEvent.ChangeTaskPriority -> selectedPriority = event.priority
            is BoardEvent.ChangeTaskPriorityConfirm -> onChangeTaskPriorityConfirm(event.task)
            is BoardEvent.ToggleTaskCompleted -> onToggleTaskCompleted(event.task)
            BoardEvent.DeleteCompletedTasks -> onDeleteCompletedTasks()
            is BoardEvent.ShowScreenMenu -> showScreenMenu = event.show
            BoardEvent.ToggleCompletedTasksVisibility -> onShowCompletedTasksChange()
            is BoardEvent.ShowDialog -> onShowDialogChange(event.type)
            BoardEvent.RefreshBoard -> {
                fetchBoard()
                fetchBoardState()
            }
            is BoardEvent.ShowTaskDateTimeFullDialog -> onShowTaskDateTimeFullDialog(event.task)
            is BoardEvent.ChangeTaskDate -> selectedDate = event.date
            is BoardEvent.ChangeTaskTime -> selectedTime = event.time
            is BoardEvent.SaveTaskDateTimeChanges -> onSaveTaskDateTimeChanges()
            is BoardEvent.ChangeNewTaskName -> newTaskNameState = newTaskNameState.copy(
                value = event.name
            )
            BoardEvent.SaveTask -> onSaveTask()
            is BoardEvent.ReorderTasks -> onReorderTasks(event.from, event.to)
            is BoardEvent.SaveTasksOrder -> onSaveTasksOrder(event.from, event.to)
            is BoardEvent.DragContent -> draggingContent = event.content
            is BoardEvent.ReorderSubtasks -> onReorderSubtasks(event.from, event.to)
            is BoardEvent.SaveSubtasksOrder -> onSaveSubtasksOrder(event.from, event.to)
        }
    }

    private fun onSaveTasksOrder(from: Int, to: Int) = viewModelScope.launch {
        if (from == to) return@launch
        if (tasks.isEmpty()) return@launch
        when (taskUseCases.arrangeBoardTasks(tasks)) {
            is Result.Error -> {
                _eventFlow.emit(Event.ShowError(
                    error = R.string.arrange_tasks_error
                ))
            }
            is Result.Success -> Unit
        }
    }

    private fun onSaveSubtasksOrder(from: Int, to: Int) = viewModelScope.launch {
        if (from == to) {
            relatedTaskId = null
            return@launch
        }
        if (recentlyReorderedSubtasks.isEmpty()) return@launch
        subtaskUseCases.arrangeSubtasks(recentlyReorderedSubtasks)
        recentlyReorderedSubtasks = emptyList()
        relatedTaskId = null
    }

    fun canDragTask(position: ItemPosition): Boolean {
        return if (tasks.isEmpty()) false
        else tasks.filterUncompleted().any { it.task.taskId == position.key }
    }

    fun canDragSubtask(position: ItemPosition): Boolean {
        val subtasks = tasks.flatMap { it.subtasks }
        val subtask = subtasks.find { it.subtaskId == position.key } ?: return false

        if (relatedTaskId == null) {
            relatedTaskId = subtask.taskId
        }

        return subtasks.filter {
            it.taskId == relatedTaskId
        }.any { it.subtaskId == position.key }
    }

    private fun onReorderTasks(from: ItemPosition, to: ItemPosition) {
        if (tasks.isEmpty()) return
        val uncompletedTasks = tasks.filterUncompleted().toMutableList().apply {
            add(
                index = indexOfFirst { it.task.taskId == to.key },
                element = removeAt(indexOfFirst { it.task.taskId == from.key })
            )
        }
        val completedTasks = tasks.filterCompleted()
        tasks = uncompletedTasks + completedTasks
    }

    private fun onReorderSubtasks(from: ItemPosition, to: ItemPosition) {
        val fromSubtask = tasks.flatMap { it.subtasks }.first { it.subtaskId == from.key }

        val subtasks = tasks.flatMap { it.subtasks }.filter {
            it.taskId == fromSubtask.taskId
        }.toMutableList().apply {
            if (isEmpty()) return
            add(
                index = indexOfFirst {
                    it.subtaskId == to.key
                },
                element = removeAt(indexOfFirst {
                    it.subtaskId == from.key
                })
            )
        }.also { recentlyReorderedSubtasks = it }

        tasks = tasks.toMutableList().apply {
            val task = first { it.task.taskId == fromSubtask.taskId }
            add(
                index = indexOfFirst { it.task.taskId == task.task.taskId },
                element = removeAt(indexOfFirst {
                    it.task.taskId == task.task.taskId
                }).copy(subtasks = subtasks)
            )
        }
    }

    private fun onDeleteCompletedTasks() = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            val completedTasks = tasks.filterCompleted().map { it.task }
            when (taskUseCases.deleteTask(*completedTasks.toTypedArray())) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.delete_completed_tasks_error
                    ))
                }
                is Result.Success -> fetchBoard()
            }
        }
    }

    private fun onToggleTaskCompleted(taskWithSubtasks: TaskWithSubtasks) = viewModelScope.launch {
        val completed = !taskWithSubtasks.task.completed
        val updatedTask = taskWithSubtasks.copy(
            task = taskWithSubtasks.task.copy(
                completed = completed,
                completedAt = if (completed) LocalDateTime.now() else null
            )
        )
        when (taskUseCases.toggleTaskCompleted(updatedTask)) {
            is Result.Error -> {
                _eventFlow.emit(Event.ShowError(
                    error = R.string.update_task_error
                ))
            }
            is Result.Success -> {
                fetchBoard()
                _eventFlow.emit(Event.Snackbar.UndoTaskCompletedToggle(
                    message = if (completed)
                        R.string.task_marked_as_completed
                    else
                        R.string.task_marked_as_uncompleted,
                    task = taskWithSubtasks
                ))
            }
        }
    }

    private fun onSaveTask() = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            TaskWithSubtasks(
                task = Task(
                    boardId = board.boardId,
                    name = newTaskNameState.value
                )
            ).let {
                when (taskUseCases.createTask(it)) {
                    is Result.Error -> {
                        _eventFlow.emit(Event.ShowError(
                            error = R.string.create_task_error
                        ))
                    }
                    is Result.Success -> fetchBoard()
                }
            }
        }
    }

    private fun onShowTaskDateTimeFullDialog(task: Task?) {
        selectedTask = task
        selectedDate = task?.date
        selectedTime = task?.time
    }

    private fun onSaveTaskDateTimeChanges() = viewModelScope.launch {
        if (selectedTask == null) return@launch
        with(selectedTask!!) {
            taskUseCases.updateTask(
                copy(time = selectedTime, date = selectedDate)
            ).also { fetchBoard() }
        }
    }

    private fun onShowDialogChange(dialogType: BoardEvent.DialogType?) {
        this.dialogType = dialogType
        selectedPriority = if (dialogType is BoardEvent.DialogType.Priority) {
            dialogType.taskWithSubtasks.priority
        } else null
    }

    private fun onChangeTaskPriorityConfirm(task: Task) = viewModelScope.launch {
        if (task.priority == selectedPriority) return@launch
        taskUseCases.updateTask(
            task.copy(priority = selectedPriority)
        ).also { fetchBoard() }
    }

    private fun onShowCompletedTasksChange() {
        showCompletedTasks = !showCompletedTasks
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name, showCompletedTasks)
            apply()
        }
    }

    private fun fetchBoard() {
        savedStateHandle.get<Int>(Screen.BOARD_ROUTE_ID_ARGUMENT)?.let { boardId ->
             viewModelScope.launch {
                 when (val result = boardUseCases.findOneBoard(boardId)) {
                     is Result.Error -> {
                         _eventFlow.emit(Event.ShowError(
                             error = R.string.find_board_error
                         ))
                     }
                     is Result.Success -> {
                         board = result.data
                         tasks = result.data.tasks
                     }
                 }
            }
        }
    }

    private fun fetchBoardState() {
        savedStateHandle.get<String>(Screen.BOARD_ROUTE_STATE_ARGUMENT)?.let { state ->
            boardState = when (state) {
                BoardState.ARCHIVED.name -> BoardState.ARCHIVED
                BoardState.DELETED.name -> BoardState.DELETED
                else -> BoardState.ACTIVE
            }
        }
    }

    private fun restoreBoard() = viewModelScope.launch {
        board?.let {
            val restoredBoard = BoardWithLabelsAndTasks(
                board = it.board.copy(state = BoardState.ACTIVE, removedAt = null),
                labels = it.labels,
                tasks = it.tasks.map { task -> CompletedTask(completed = task.task.completed) }
            )
            boardUseCases.updateBoard(restoredBoard)
            _eventFlow.emit(Event.Snackbar.UndoBoardChange(
                message = R.string.board_was_restored,
                board = it
            ))
        }
    }

    private fun onDeactivateBoard(mode: DeactivateMode) = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            when (mode) {
                DeactivateMode.ARCHIVE -> {
                    BoardWithLabelsAndTasks(
                        board = board.copy(state = BoardState.ARCHIVED, removedAt = null),
                        labels = labels,
                        tasks = tasks.map { CompletedTask(completed = it.task.completed) }
                    ).let { boardUseCases.updateBoard(it) }
                    _eventFlow.emit(Event.Snackbar.UndoBoardChange(
                        message = R.string.board_archived,
                        board = this,
                    ))
                }
                DeactivateMode.DELETE -> {
                    BoardWithLabelsAndTasks(
                        board = board.copy(
                            state = BoardState.DELETED,
                            removedAt = LocalDateTime.now()
                        ),
                        labels = labels,
                        tasks = tasks.map { CompletedTask(completed = it.task.completed) }
                    ).let { boardUseCases.updateBoard(it) }
                    _eventFlow.emit(Event.Snackbar.UndoBoardChange(
                        message = R.string.board_moved_to_trash,
                        board = this@with
                    ))
                }
            }
        }
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        sealed class Snackbar : Event() {
            data class UndoBoardChange(
                @StringRes val message: Int,
                val board: DetailedBoard,
            ) : Snackbar()

            data class UndoTaskCompletedToggle(
                @StringRes val message: Int,
                val task: TaskWithSubtasks
            ) : Snackbar()
        }
    }
}