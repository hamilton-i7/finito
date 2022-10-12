package com.example.finito.features.tasks.presentation.screen.today

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.util.PreferencesKeys
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.filterCompleted
import com.example.finito.features.subtasks.domain.usecase.SubtaskUseCases
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
    private val subtaskUseCases: SubtaskUseCases,
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
            PreferencesKeys.TASKS_ORDER,
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
        PreferencesKeys.SHOW_COMPLETED_TASKS,
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

    var selectedTask by mutableStateOf<Task?>(null)
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
            is TodayEvent.ToggleSubtaskCompleted -> onToggleSubtaskCompleted(event.subtask)
            TodayEvent.ResetBottomSheetContent -> onResetBottomSheetContent()
            is TodayEvent.ChangeBottomSheetContent -> onShowBottomSheetContent(event.content)
            TodayEvent.DismissBottomSheet -> onDismissBottomSheet()
        }
    }

    private fun onDismissBottomSheet() {
        newTaskNameState = newTaskNameState.copy(value = "")
        selectedBoard = boards.firstOrNull()
    }

    private fun onShowBottomSheetContent(content: TodayEvent.BottomSheetContent) {
        bottomSheetContent = content
        if (content !is TodayEvent.BottomSheetContent.BoardsList) return
        selectedBoard = content.task?.let { task ->
            boards.first { it.boardId == task.boardId }
        } ?: selectedBoard
    }

    private fun onShowDialog(type: TodayEvent.DialogType?) {
        dialogType = type
        selectedPriority = if (type is TodayEvent.DialogType.Priority) {
            type.task.priority
        } else null
    }

    private fun onSortByPriority(option: SortingOption.Priority?) {
        sortingOption = option
        with(preferences.edit()) {
            putString(PreferencesKeys.TASKS_ORDER, option?.name)
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
        when (taskUseCases.toggleTaskCompleted(updatedTask)) {
            is Result.Error -> {
                _eventFlow.emit(Event.ShowError(
                    error = R.string.update_task_error
                ))
            }
            is Result.Success -> {
                _eventFlow.emit(Event.Snackbar.UndoTaskCompletedToggle(
                    message = if (completed)
                        R.string.task_marked_as_completed
                    else
                        R.string.task_marked_as_uncompleted,
                    task = task
                ))
            }
        }
    }

    private fun onToggleSubtaskCompleted(subtask: Subtask) = viewModelScope.launch {
        val completed = !subtask.completed
        val updatedSubtask = subtask.copy(
            completed = completed,
            completedAt = if (completed) LocalDateTime.now() else null,
        )
        when (subtaskUseCases.updateSubtask(updatedSubtask)) {
            is Result.Error -> {
                _eventFlow.emit(Event.ShowError(
                    error = R.string.update_task_error
                ))
            }
            is Result.Success -> {
                _eventFlow.emit(Event.Snackbar.UndoSubtaskCompletedToggle(
                    message = if (completed)
                        R.string.subtask_marked_as_completed
                    else
                        R.string.subtask_marked_as_uncompleted,
                    subtask = subtask,
                    task = tasks.first { it.task.taskId == subtask.taskId }.task
                ))
            }
        }
    }

    private fun onShowCompletedTasksChange() {
        showCompletedTasks = !showCompletedTasks
        with(preferences.edit()) {
            putBoolean(PreferencesKeys.SHOW_COMPLETED_TASKS, showCompletedTasks)
            apply()
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

    private fun onDeleteCompletedTasks() {
        deleteCompletedTasks()
        deleteCompletedSubtasks()
    }

    private fun deleteCompletedTasks() = viewModelScope.launch {
        val completedTasks = tasks.filterCompleted().map { it.task }
        if (taskUseCases.deleteTask(*completedTasks.toTypedArray()) is Result.Error) {
            _eventFlow.emit(Event.ShowError(
                error = R.string.delete_completed_tasks_error
            ))
        }
    }


    private fun deleteCompletedSubtasks() = viewModelScope.launch {
        val completedSubtasks = tasks.flatMap { it.subtasks }.filterCompleted()
        if (subtaskUseCases.deleteSubtask(*completedSubtasks.toTypedArray()) is Result.Error) {
            _eventFlow.emit(Event.ShowError(
                error = R.string.delete_completed_subtasks_error
            ))
        }
    }

    private fun onChangeTaskPriorityConfirm(task: Task) = viewModelScope.launch {
        if (task.priority == selectedPriority) return@launch
        val updatedTask = task.copy(priority = selectedPriority)
        taskUseCases.updateTask(updatedTask)
    }

    private fun onChangeBoard(
        board: SimpleBoard,
        task: Task?,
    ) = viewModelScope.launch {
        if (task == null) {
            selectedBoard = board
            return@launch
        }
        val updatedTask = task.copy(boardId = board.boardId)
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
            data class UndoTaskCompletedToggle(
                @StringRes val message: Int,
                val task: TaskWithSubtasks
            ) : Snackbar()

            data class UndoSubtaskCompletedToggle(
                @StringRes val message: Int,
                val subtask: Subtask,
                val task: Task,
            ) : Snackbar()
        }
    }
}