package com.example.finito.features.tasks.presentation.screen.addedittask

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.domain.Priority
import com.example.finito.core.domain.Reminder
import com.example.finito.core.domain.Result
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val boardUseCases: BoardUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var task by mutableStateOf<TaskWithSubtasks?>(null)
        private set

    var boards by mutableStateOf<List<SimpleBoard>>(emptyList())
        private set

    var nameState by mutableStateOf(TextFieldState())
        private set

    var descriptionState by mutableStateOf(TextFieldState())
        private set

    var selectedBoard by mutableStateOf<SimpleBoard?>(null)
        private set

    var selectedReminder by mutableStateOf<Reminder?>(null)
        private set

    var selectedPriority by mutableStateOf<Priority?>(null)
        private set

    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var selectedTime by mutableStateOf<LocalTime?>(null)
        private set

    var dialogType by mutableStateOf<AddEditTaskEvent.DialogType?>(null)
        private set

    var subtaskNameStates by mutableStateOf<List<TextFieldState>>(emptyList())
        private set

    var showReminders by mutableStateOf(false)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var subtaskNameStateId = -1

    init {
        fetchTask()
        fetchBoards()
    }

    fun onEvent(event: AddEditTaskEvent) {
        when (event) {
            is AddEditTaskEvent.ChangeBoard -> selectedBoard = event.board
            is AddEditTaskEvent.ChangeDate -> selectedDate = event.date
            is AddEditTaskEvent.ChangeTime -> selectedTime = event.time
            is AddEditTaskEvent.ChangeName -> nameState = nameState.copy(value = event.name)
            is AddEditTaskEvent.ChangeDescription -> {
                descriptionState = descriptionState.copy(value = event.description)
            }
            is AddEditTaskEvent.ChangePriority -> selectedPriority = event.priority
            is AddEditTaskEvent.ChangeReminder -> selectedReminder = event.reminder
            AddEditTaskEvent.CreateSubtask -> onCreateSubtask()
            is AddEditTaskEvent.RemoveSubtask -> onRemoveTask(event.state)
            AddEditTaskEvent.CreateTask -> onCreateTask()
            AddEditTaskEvent.EditTask -> onEditTask()
            AddEditTaskEvent.DeleteTask -> onDeleteTask()
            is AddEditTaskEvent.ReorderSubtasks -> onReorderSubtasks(event.from, event.to)
            is AddEditTaskEvent.ShowDialog -> dialogType = event.type
            AddEditTaskEvent.ToggleCompleted -> onToggleCompleted()
            is AddEditTaskEvent.ShowReminders -> showReminders = event.show
        }
    }

    fun canDragTask(position: ItemPosition): Boolean {
        if (subtaskNameStates.isEmpty()) return false
        return subtaskNameStates.any { it.id == position.key }
    }

    private fun onToggleCompleted() = viewModelScope.launch {
        if (task == null) return@launch
        with(task!!) {
            val completed = !task.completed
            val updatedTask = copy(task = task.copy(
                completed = completed,
                completedAt = if (completed) LocalDateTime.now() else null
            ))
            when (taskUseCases.updateTask(updatedTask)) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.update_task_error
                    ))
                }
                is Result.Success -> {
                    _eventFlow.emitAll(
                        flow {
                            emit(Event.NavigateToBoard(selectedBoard!!.boardId))
                            emit(Event.ShowSnackbar(
                                message = if (completed)
                                    R.string.task_marked_as_completed
                                else
                                    R.string.task_marked_as_uncompleted
                            ))
                        }
                    )
                }
            }
        }
    }

    private fun onReorderSubtasks(from: ItemPosition, to: ItemPosition) {
        if (subtaskNameStates.isEmpty()) return
        subtaskNameStates = subtaskNameStates.toMutableList().apply {
            add(
                index = indexOfFirst { it.id == to.key },
                element = removeAt(indexOfFirst { it.id == from.key })
            )
        }
    }

    private fun onDeleteTask() = viewModelScope.launch {
        if (task == null) return@launch
        with(task!!) {
            when (taskUseCases.deleteTask(task)) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.delete_task_error
                    ))
                }
                is Result.Success -> {
                    _eventFlow.emit(Event.NavigateToBoard(id = selectedBoard!!.boardId))
                }
            }
        }
    }

    private fun onEditTask() = viewModelScope.launch {
        if (task == null) return@launch
        with(task!!) {
            TaskWithSubtasks(
                task = task.copy(
                    boardId = selectedBoard!!.boardId,
                    name = nameState.value,
                    description = descriptionState.value.ifBlank { null },
                    date = selectedDate,
                    time = selectedTime,
                    reminder = selectedReminder,
                    priority = selectedPriority,
                ),
                subtasks = subtaskNameStates.map {
                    // If it's a new Subtask, create a new instance
                    // Otherwise, simply update its name
                    if (subtasks.indexOfFirst { subtask -> subtask.subtaskId == it.id } == -1) {
                        Subtask(name = it.value)
                    }
                    subtasks.first { subtask -> subtask.subtaskId == it.id }.copy(name = it.value)
                }
            ).let {
                when (taskUseCases.updateTask(it)) {
                    is Result.Error -> {
                        _eventFlow.emit(Event.ShowError(
                            error = R.string.update_task_error
                        ))
                    }
                    is Result.Success -> {
                        _eventFlow.emit(Event.NavigateToBoard(it.task.boardId))
                    }
                }
            }
        }
    }

    private fun onCreateTask() = viewModelScope.launch {
        TaskWithSubtasks(
            task = Task(
                boardId = selectedBoard!!.boardId,
                name = nameState.value,
                description = descriptionState.value.ifBlank { null },
                date = selectedDate,
                time = selectedTime,
                reminder = selectedReminder,
                priority = selectedPriority,
            ),
            subtasks = subtaskNameStates.map { Subtask(name = it.value) }
        )
    }

    private fun onCreateSubtask() {
        subtaskNameStates = subtaskNameStates + listOf(TextFieldState(id = ++subtaskNameStateId))
    }

    private fun onRemoveTask(state: TextFieldState) {
        subtaskNameStates = subtaskNameStates.filter { it.id != state.id }
    }

    private fun fetchTask() {
        savedStateHandle.get<Int>(Screen.EDIT_TASK_ROUTE_ID_ARGUMENT)?.let { taskId ->
            viewModelScope.launch {
                when (val result = taskUseCases.findOneTask(taskId)) {
                    is Result.Error -> TODO()
                    is Result.Success -> setupData(result.data)
                }
            }
        }
    }

    private fun fetchBoards() = viewModelScope.launch {
        boardUseCases.findSimpleBoards().data.onEach { boards ->
            this@AddEditTaskViewModel.boards = boards
            val boardId = savedStateHandle.get<Int>(Screen.BOARD_ROUTE_ID_ARGUMENT) ?: return@onEach
            boards.first { it.boardId == boardId }.let {
                selectedBoard = it
            }
        }.launchIn(viewModelScope)
    }
    
    private suspend fun setupData(taskWithSubtasks: TaskWithSubtasks) {
         this.task = taskWithSubtasks
        boardUseCases.findOneBoard(taskWithSubtasks.task.boardId).let { result ->
            when (result) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.find_board_error
                    ))
                }
                is Result.Success -> {
                    val board = result.data.board
                    selectedBoard = SimpleBoard(boardId = board.boardId, name = board.name)
                }
            }
        }
        nameState = nameState.copy(value = taskWithSubtasks.task.name)
        if (taskWithSubtasks.task.description != null) {
            descriptionState = descriptionState.copy(value = taskWithSubtasks.task.description)
        }
        selectedDate = taskWithSubtasks.task.date
        selectedTime = taskWithSubtasks.task.time
        selectedReminder = taskWithSubtasks.task.reminder
        selectedPriority = taskWithSubtasks.task.priority
        subtaskNameStates = taskWithSubtasks.subtasks.map {
            TextFieldState(
                id = it.subtaskId,
                value = it.name
            )
        }
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        data class NavigateToBoard(val id: Int) : Event()

        data class ShowSnackbar(@StringRes val message: Int) : Event()
    }
}