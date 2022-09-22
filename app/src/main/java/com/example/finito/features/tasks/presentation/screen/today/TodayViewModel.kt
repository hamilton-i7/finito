package com.example.finito.features.tasks.presentation.screen.today

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.Priority
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val boardUseCases: BoardUseCases,
    private val preferences: SharedPreferences
) : ViewModel() {

    var boards by mutableStateOf<List<SimpleBoard>>(emptyList())
        private set

    var boardsMap by mutableStateOf<Map<Int, String>>(mapOf())
        private set

    var tasks by mutableStateOf<List<TaskWithSubtasks>>(emptyList())
        private set

    private var fetchTasksJob: Job? = null

    var sortingOption by mutableStateOf(
        value = preferences.getString(
            PreferencesModule.TAG.TASKS_ORDER.name,
            null
        )?.let {
            when (it) {
                SortingOption.Priority.LeastUrgent.name -> SortingOption.Priority.LeastUrgent
                SortingOption.Priority.MostUrgent.name -> SortingOption.Priority.MostUrgent
                else -> null
            }
        }
    ); private set

    var showScreenMenu by mutableStateOf(false)
        private set

    var showCompletedTasks by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name,
        true
    )); private set

    var dialogType by mutableStateOf<TodayEvent.DialogType?>(null)
        private set

    var newTaskNameState by mutableStateOf(TextFieldState())
        private set

    var selectedPriority by mutableStateOf<Priority?>(null)
        private set

    var selectedBoard by mutableStateOf<SimpleBoard?>(null)
        private set

    var selectedTask by mutableStateOf<TaskWithSubtasks?>(null)
        private set

    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var selectedTime by mutableStateOf<LocalTime?>(null)
        private set

    var bottomSheetContent by mutableStateOf<TodayEvent.BottomSheetContent>(
        TodayEvent.BottomSheetContent.NewTask
    ); private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchTasks()
        fetchBoards()
    }

    fun onEvent(event: TodayEvent) {
        when (event) {
            is TodayEvent.ChangeBoard -> onChangeBoard(event.board, event.task)
            is TodayEvent.ChangeNewTaskName -> newTaskNameState = newTaskNameState.copy(
                value = event.name
            )
            is TodayEvent.ChangeTaskPriority -> selectedPriority = event.priority
            is TodayEvent.ChangeTaskPriorityConfirm -> onChangeTaskPriorityConfirm(event.task)
            TodayEvent.DeleteCompleted -> onDeleteCompletedTasks()
            TodayEvent.SaveNewTask -> onSaveTask()
            is TodayEvent.ShowTaskDateTimeFullDialog -> onShowTaskDateTimeFullDialog(event.task)
            is TodayEvent.ChangeDate -> selectedDate = event.date
            is TodayEvent.ChangeTime -> selectedTime = event.time
            TodayEvent.SaveTaskDateTimeChanges -> onSaveTaskDateTimeChanges()
            is TodayEvent.ShowDialog -> onShowDialog(event.type)
            is TodayEvent.ShowScreenMenu -> showScreenMenu = event.show
            is TodayEvent.SortByPriority -> onSortByPriority(event.option)
            TodayEvent.ToggleCompletedTasksVisibility -> onShowCompletedTasksChange()
            is TodayEvent.ToggleTaskCompleted -> onToggleTaskCompleted(event.task)
            TodayEvent.ResetBottomSheetContent -> onResetBottomSheetContent()
            is TodayEvent.ChangeBottomSheetContent -> bottomSheetContent = event.content
        }
    }

    private fun onShowDialog(type: TodayEvent.DialogType?) {
        dialogType = type
        selectedPriority = if (type is TodayEvent.DialogType.Priority) {
            type.task.task.priority
        } else null
    }

    private fun onSortByPriority(option: SortingOption.Priority?) {
        sortingOption = option
        with(preferences.edit()) {
            putString(PreferencesModule.TAG.TASKS_ORDER.name, option?.name)
            apply()
        }
        fetchTasks()
    }

    private fun onResetBottomSheetContent() {
        selectedBoard = boards.first()
        newTaskNameState = newTaskNameState.copy(value = "")
    }

    private fun onToggleTaskCompleted(task: TaskWithSubtasks) = viewModelScope.launch {
        val completed = !task.task.completed
        val updatedTask = task.copy(
            task = task.task.copy(
                completed = completed,
                completedAt = if (completed) LocalDateTime.now() else null
            )
        )
        when (taskUseCases.updateTask(updatedTask)) {
            is Result.Error -> {
                _eventFlow.emit(Event.ShowError(
                    error = R.string.update_task_error
                ))
            }
            is Result.Success -> {
                _eventFlow.emit(Event.Snackbar.UndoTaskChange(
                    message = if (completed)
                        R.string.task_marked_as_completed
                    else
                        R.string.task_marked_as_uncompleted,
                    task = task
                ))
            }
        }
    }

    private fun onShowCompletedTasksChange() {
        showCompletedTasks = !showCompletedTasks
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name, showCompletedTasks)
            apply()
        }
    }

    private fun onShowTaskDateTimeFullDialog(task: TaskWithSubtasks?) {
        selectedTask = task
        selectedDate = task?.task?.date
        selectedTime = task?.task?.time
    }

    private fun onSaveTaskDateTimeChanges() = viewModelScope.launch {
        if (selectedTask == null) return@launch
        with(selectedTask!!) {
            taskUseCases.updateTask(
                copy(task = task.copy(time = selectedTime, date = selectedDate))
            )
        }
    }

    private fun onSaveTask() = viewModelScope.launch {
        TaskWithSubtasks(
            task = Task(
                boardId = selectedBoard!!.boardId,
                name = newTaskNameState.value
            )
        ).let {
            when (taskUseCases.createTask(it)) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.create_task_error
                    ))
                }
                is Result.Success -> Unit
            }
        }
    }

    private fun onDeleteCompletedTasks() = viewModelScope.launch {
        val completedTasks = tasks.filterCompleted().map { it.task }
        when (taskUseCases.deleteTask(*completedTasks.toTypedArray())) {
            is Result.Error -> {
                _eventFlow.emit(Event.ShowError(
                    error = R.string.delete_completed_tasks_error
                ))
            }
            is Result.Success -> Unit
        }
    }

    private fun onChangeTaskPriorityConfirm(task: TaskWithSubtasks) = viewModelScope.launch {
        if (task.task.priority == selectedPriority) return@launch
        val updatedTask = task.copy(
            task = task.task.copy(priority = selectedPriority)
        )
        taskUseCases.updateTask(updatedTask)
    }

    private fun onChangeBoard(
        board: SimpleBoard,
        task: TaskWithSubtasks?,
    ) = viewModelScope.launch {
        if (task == null) {
            selectedBoard = board
            return@launch
        }
        val updatedTask = task.copy(
            task = task.task.copy(boardId = board.boardId)
        )
        taskUseCases.updateTask(updatedTask)
    }

    private fun fetchTasks() = viewModelScope.launch {
        fetchTasksJob?.cancel()
        fetchTasksJob = taskUseCases.findTodayTasks(taskOrder = sortingOption).data.onEach { tasks ->
            this@TodayViewModel.tasks = tasks
        }.launchIn(viewModelScope)
    }

    private fun fetchBoards() = viewModelScope.launch {
        boardUseCases.findSimpleBoards().data.onEach { boards ->
            this@TodayViewModel.boards = boards
            selectedBoard = boards.first()
            boardsMap = mutableMapOf<Int, String>().apply {
                boards.forEach { board ->
                    set(board.boardId, board.name)
                }
            }
        }.launchIn(viewModelScope)
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        sealed class Snackbar : Event() {
            data class UndoTaskChange(
                @StringRes val message: Int,
                val task: TaskWithSubtasks
            ) : Snackbar()
        }
    }
}