package com.example.finito.features.tasks.presentation.screen.tomorrow

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
class TomorrowViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val boardUseCases: BoardUseCases,
    private val preferences: SharedPreferences
) : ViewModel() {

    var boards by mutableStateOf<List<SimpleBoard>>(emptyList())
        private set

    var boardNamesMap by mutableStateOf<Map<Int, String>>(mapOf())
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

    var dialogType by mutableStateOf<TomorrowEvent.DialogType?>(null)
        private set

    var newTaskNameState by mutableStateOf(TextFieldState())
        private set

    var selectedPriority by mutableStateOf<Priority?>(null)
        private set

    var selectedBoard by mutableStateOf<SimpleBoard?>(null)
        private set

    var selectedTask by mutableStateOf<Task?>(null)
        private set

    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var selectedTime by mutableStateOf<LocalTime?>(null)
        private set

    var bottomSheetContent by mutableStateOf<TomorrowEvent.BottomSheetContent>(
        TomorrowEvent.BottomSheetContent.NewTask
    ); private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchTasks()
        fetchBoards()
    }

    fun onEvent(event: TomorrowEvent) {
        when (event) {
            is TomorrowEvent.ChangeBoard -> onChangeBoard(event.board, event.task)
            is TomorrowEvent.ChangeNewTaskName -> newTaskNameState = newTaskNameState.copy(
                value = event.name
            )
            is TomorrowEvent.ChangeTaskPriority -> selectedPriority = event.priority
            is TomorrowEvent.ChangeTaskPriorityConfirm -> onChangeTaskPriorityConfirm(event.task)
            TomorrowEvent.DeleteCompleted -> onDeleteCompletedTasks()
            TomorrowEvent.SaveNewTask -> onSaveTask()
            is TomorrowEvent.ShowTaskDateTimeFullDialog -> onShowTaskDateTimeFullDialog(event.task)
            is TomorrowEvent.ChangeDate -> selectedDate = event.date
            is TomorrowEvent.ChangeTime -> selectedTime = event.time
            TomorrowEvent.SaveTaskDateTimeChanges -> onSaveTaskDateTimeChanges()
            is TomorrowEvent.ShowDialog -> onShowDialog(event.type)
            is TomorrowEvent.ShowScreenMenu -> showScreenMenu = event.show
            is TomorrowEvent.SortByPriority -> onSortByPriority(event.option)
            TomorrowEvent.ToggleCompletedTasksVisibility -> onShowCompletedTasksChange()
            is TomorrowEvent.ToggleTaskCompleted -> onToggleTaskCompleted(event.task)
            TomorrowEvent.ResetBottomSheetContent -> onResetBottomSheetContent()
            is TomorrowEvent.ChangeBottomSheetContent -> onShowBottomSheetContent(event.content)
            TomorrowEvent.DismissBottomSheet -> onDismissBottomSheet()
        }
    }

    private fun onDismissBottomSheet() {
        newTaskNameState = newTaskNameState.copy(value = "")
        selectedBoard = boards.firstOrNull()
    }

    private fun onShowBottomSheetContent(content: TomorrowEvent.BottomSheetContent) {
        bottomSheetContent = content
        if (content !is TomorrowEvent.BottomSheetContent.BoardsList) return
        selectedBoard = content.task?.let { task ->
            boards.first { it.boardId == task.task.boardId }
        } ?: selectedBoard
    }

    private fun onShowDialog(type: TomorrowEvent.DialogType?) {
        dialogType = type
        selectedPriority = if (type is TomorrowEvent.DialogType.Priority) {
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
        selectedTask = task?.task
        selectedDate = task?.task?.date
        selectedTime = task?.task?.time
    }

    private fun onSaveTaskDateTimeChanges() = viewModelScope.launch {
        if (selectedTask == null) return@launch
        with(selectedTask!!) {
            taskUseCases.updateTask(
                copy(time = selectedTime, date = selectedDate)
            )
        }
    }

    private fun onSaveTask() = viewModelScope.launch {
        TaskWithSubtasks(
            task = Task(
                boardId = selectedBoard!!.boardId,
                name = newTaskNameState.value,
                date = LocalDate.now()
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
        fetchTasksJob = taskUseCases.findTomorrowTasks(taskOrder = sortingOption).data.onEach { tasks ->
            this@TomorrowViewModel.tasks = tasks
        }.launchIn(viewModelScope)
    }

    private fun fetchBoards() = viewModelScope.launch {
        boardUseCases.findSimpleBoards().data.onEach { boards ->
            this@TomorrowViewModel.boards = boards
            selectedBoard = boards.first()
            boardNamesMap = mutableMapOf<Int, String>().apply {
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