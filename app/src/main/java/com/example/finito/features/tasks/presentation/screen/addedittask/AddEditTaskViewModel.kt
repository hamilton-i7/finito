package com.example.finito.features.tasks.presentation.screen.addedittask

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.domain.Result
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.RequestCodes
import com.example.finito.core.presentation.util.SubtaskTextField
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.features.tasks.presentation.util.TaskReminderAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val application: Application,
    private val taskUseCases: TaskUseCases,
    private val boardUseCases: BoardUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var task by mutableStateOf<TaskWithSubtasks?>(null)
        private set

    var boards by mutableStateOf<List<SimpleBoard>>(emptyList())
        private set

    var nameState by mutableStateOf(TextFieldState.Default)
        private set

    var descriptionState by mutableStateOf(TextFieldState.Default)
        private set

    var selectedBoard by mutableStateOf<SimpleBoard?>(null)
        private set

    var selectedPriority by mutableStateOf<Priority?>(null)
        private set

    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var selectedTime by mutableStateOf<LocalTime?>(null)
        private set

    var dialogType by mutableStateOf<AddEditTaskEvent.DialogType?>(null)
        private set

    var subtaskNameStates by mutableStateOf<List<SubtaskTextField>>(emptyList())
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    var originalRelatedBoard: Board? = null
        private set

    private var subtaskNameStateId = -1

    private val postNotificationsPermissionGranted =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            mutableStateOf(false)
        else
            null

    var shouldCheckPostNotificationsPermission by mutableStateOf(false)
        private set

    var firstTimeAskingNotificationsPermission by mutableStateOf(
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    ); private set


    init {
        fetchTask()
        fetchBoards()
        fetchNameState()
        fetchDateState()
        fetchPriorityState()
    }

    fun onEvent(event: AddEditTaskEvent) {
        when (event) {
            is AddEditTaskEvent.ChangeBoard -> selectedBoard = event.board
            is AddEditTaskEvent.ChangeDate -> onChangeDate(event.date)
            is AddEditTaskEvent.ChangeTime -> selectedTime = event.time
            is AddEditTaskEvent.ChangeName -> nameState = nameState.copy(value = event.name)
            is AddEditTaskEvent.ChangeDescription -> {
                descriptionState = descriptionState.copy(value = event.description)
            }
            is AddEditTaskEvent.ChangePriority -> selectedPriority = event.priority
            AddEditTaskEvent.CreateSubtask -> onCreateSubtask()
            is AddEditTaskEvent.RemoveSubtask -> onRemoveSubtask(event.state)
            AddEditTaskEvent.CreateTask -> onCreateTask()
            AddEditTaskEvent.EditTask -> onEditTask()
            AddEditTaskEvent.DeleteTask -> onDeleteTask()
            is AddEditTaskEvent.ReorderSubtasks -> onReorderSubtasks(event.from, event.to)
            is AddEditTaskEvent.ShowDialog -> dialogType = event.type
            AddEditTaskEvent.ToggleCompleted -> onToggleCompleted()
            is AddEditTaskEvent.ChangeSubtaskName -> onChangeSubtaskName(event.id, event.name)
            AddEditTaskEvent.RefreshTask -> fetchTask()
            AddEditTaskEvent.AllowReminder -> onAllowReminder()
            AddEditTaskEvent.SkipNotificationsPermissionCheck -> firstTimeAskingNotificationsPermission = false
        }
    }

    private fun onAllowReminder() {
        postNotificationsPermissionGranted!!.value = true
    }

    private fun onChangeDate(date: LocalDate?) = viewModelScope.launch {
        selectedDate = date
        shouldCheckPostNotificationsPermission = date != null && firstTimeAskingNotificationsPermission
    }

    private fun onChangeSubtaskName(stateId: Int, name: String) {
        subtaskNameStates = subtaskNameStates.toMutableList().apply {
            add(
                index = indexOfFirst { it.id == stateId },
                element = removeAt(indexOfFirst { it.id == stateId }).copy(value = name)
            )
        }
    }

    fun canDragTask(position: ItemPosition): Boolean {
        if (subtaskNameStates.isEmpty()) return false
        return subtaskNameStates.any { it.id == position.key }
    }

    private fun onToggleCompleted() = viewModelScope.launch {
        if (task == null) return@launch
        with(task!!) {
            boardUseCases.findOneBoard(task.boardId).let {
                if (it is Result.Error) return@launch
                val board = (it as Result.Success).data
                val uncompletedTasksAmount = board.tasks.count { task -> !task.task.completed }
                val completed = !task.completed
                val updatedTask = copy(task = task.copy(
                    completed = completed,
                    completedAt = if (completed) LocalDateTime.now() else null,
                    boardPosition = if (completed) null else uncompletedTasksAmount
                ))

                when (taskUseCases.updateTask(updatedTask)) {
                    is Result.Error -> {
                        fireEvents(Event.ShowError(
                            error = R.string.update_task_error
                        ))
                    }
                    is Result.Success -> {
                        fireEvents(
                            Event.Snackbar.TaskStateChanged(
                                message = if (completed)
                                    R.string.task_marked_as_completed
                                else
                                    R.string.task_marked_as_uncompleted,
                                task = this@with
                            ),
                            Event.NavigateBack
                        )
                    }
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
                    fireEvents(Event.ShowError(
                        error = R.string.delete_task_error
                    ))
                }
                is Result.Success -> {
                    fireEvents(
                        Event.Snackbar.TaskDeleted(task = this@with),
                        Event.NavigateBack
                    )
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
                    priority = selectedPriority,
                ),
                subtasks = subtaskNameStates.filter { it.value.isNotBlank() }.map {
                    // If it's a new Subtask, create a new instance
                    // Otherwise, simply update its name
                    if (subtasks.indexOfFirst { subtask -> subtask.subtaskId == it.id } == -1) {
                        return@map Subtask(name = it.value, taskId = task.taskId)
                    }
                    subtasks.first { subtask -> subtask.subtaskId == it.id }.copy(name = it.value)
                }
            ).let {
                when (taskUseCases.updateTask(it)) {
                    is Result.Error -> {
                        fireEvents(
                            Event.ShowError(
                                error = R.string.update_task_error
                            )
                        )
                    }
                    is Result.Success -> {
                        // TODO 13/10/2022: Create reminders if need be
                        fireEvents(Event.NavigateBack)
                    }
                }
            }
        }
    }

    private fun onCreateTask() = viewModelScope.launch {
        val createdTask = TaskWithSubtasks(
            task = Task(
                boardId = selectedBoard!!.boardId,
                name = nameState.value,
                description = descriptionState.value.ifBlank { null },
                date = selectedDate,
                time = selectedTime,
                priority = selectedPriority,
            ),
            subtasks = subtaskNameStates.filter { it.value.isNotBlank() }.map { Subtask(name = it.value) }
        )
        when (val result = taskUseCases.createTask(createdTask)) {
            is Result.Error -> {
                fireEvents(
                    Event.ShowError(
                        error = R.string.create_task_error
                    )
                )
            }
            is Result.Success -> {
                if (!canCreateReminder()) {
                    fireEvents(Event.NavigateBack)
                    return@launch
                }
                onCreateTaskReminder(createdTask.task.copy(taskId = result.data))
                fireEvents(Event.NavigateBack)
            }
        }
    }

    private fun canCreateReminder(): Boolean {
        if (postNotificationsPermissionGranted?.value == false) return false
        if (selectedDate == null) return false
        if (selectedTime == null) {
            val today = LocalDate.now()
            if (selectedDate!!.isEqual(today) || selectedDate!!.isBefore(today)) return false
        }
        val reminderDateTime = LocalDateTime.of(selectedDate, selectedTime)
        val now = LocalDateTime.now()
        if (reminderDateTime.isEqual(now) || reminderDateTime.isBefore(now)) {
            return false
        }
        return true
    }

    private fun onCreateTaskReminder(task: Task) {
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(
            application.applicationContext,
            TaskReminderAlarmReceiver::class.java
        ).apply {
            putExtra(TaskReminderAlarmReceiver.EXTRA_TASK_ID, task.taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            application.applicationContext,
            RequestCodes.ALARM_REQUEST_CODE + task.taskId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or Intent.FILL_IN_DATA
        )
        val localDateTime = LocalDateTime.of(task.date, task.time ?: LocalTime.MIDNIGHT)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, localDateTime.dayOfYear)
            set(Calendar.HOUR_OF_DAY, localDateTime.hour)
            set(Calendar.MINUTE, localDateTime.minute)
            set(Calendar.SECOND, 0)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun onCreateSubtask() {
        subtaskNameStates = subtaskNameStates + listOf(SubtaskTextField(id = ++subtaskNameStateId))
    }

    private fun onRemoveSubtask(state: SubtaskTextField) {
        subtaskNameStates = subtaskNameStates.filter { it.id != state.id }
    }

    private fun fetchTask() {
        savedStateHandle.get<Int>(Screen.TASK_ID_ARGUMENT)?.let { taskId ->
            viewModelScope.launch {
                when (val result = taskUseCases.findOneTask(taskId)) {
                    is Result.Error -> {
                        fireEvents(
                            Event.ShowError(
                                error = R.string.find_task_error
                            )
                        )
                    }
                    is Result.Success -> setupData(result.data)
                }
            }
        }
    }

    private fun fetchBoards() = viewModelScope.launch {
        boardUseCases.findSimpleBoards().data.onEach { boards ->
            this@AddEditTaskViewModel.boards = boards
            val boardId = savedStateHandle.get<Int>(Screen.BOARD_ID_ARGUMENT) ?: return@onEach
            if (boardId == -1) return@onEach

            boards.first { it.boardId == boardId }.let {
                selectedBoard = it
            }
            when (val result = boardUseCases.findOneBoard(boardId)) {
                is Result.Error -> {
                    fireEvents(Event.ShowError(
                        error = R.string.find_board_error
                    ))
                }
                is Result.Success -> originalRelatedBoard = result.data.board
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchNameState() {
        savedStateHandle.get<String>(Screen.TASK_NAME_ARGUMENT)?.let { name ->
            nameState = nameState.copy(value = name)
        }
    }

    private fun fetchDateState() {
        savedStateHandle.get<String>(Screen.DATE_ARGUMENT)?.let { date ->
            if (date.isEmpty()) return@let
            selectedDate = LocalDate.parse(date)
        }
    }

    private fun fetchPriorityState() {
        savedStateHandle.get<Boolean>(Screen.IS_URGENT_ARGUMENT)?.let { isUrgent ->
            if (!isUrgent) return@let
            selectedPriority = Priority.URGENT
        }
    }
    
    private suspend fun setupData(taskWithSubtasks: TaskWithSubtasks) {
         this.task = taskWithSubtasks
        fetchRelatedBoard(taskWithSubtasks.task.boardId)
        nameState = nameState.copy(value = taskWithSubtasks.task.name)
        if (taskWithSubtasks.task.description != null) {
            descriptionState = descriptionState.copy(value = taskWithSubtasks.task.description)
        }
        selectedDate = taskWithSubtasks.task.date
        selectedTime = taskWithSubtasks.task.time
        selectedPriority = taskWithSubtasks.task.priority
        subtaskNameStates = taskWithSubtasks.subtasks.map {
            SubtaskTextField(
                id = it.subtaskId,
                value = it.name,
                completed = it.completed
            )
        }
    }

    private suspend fun fetchRelatedBoard(boardId: Int) {
        boardUseCases.findOneBoard(boardId).let { result ->
            when (result) {
                is Result.Error -> {
                    fireEvents(
                        Event.ShowError(
                            error = R.string.find_board_error
                        )
                    )
                }
                is Result.Success -> {
                    val board = result.data.board
                    selectedBoard = SimpleBoard(boardId = board.boardId, name = board.name)
                    originalRelatedBoard = result.data.board
                }
            }
        }
    }

    private suspend fun fireEvents(vararg events: Event) {
        events.forEachIndexed { index, event ->
            _eventFlow.emit(event)
            if (index != events.lastIndex) { delay(100) }
        }
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        object NavigateBack : Event()

        sealed class Snackbar(
            @StringRes open val message: Int,
            @StringRes val actionLabel: Int = R.string.undo,
        ) : Event() {
            class TaskStateChanged(
                @StringRes message: Int,
                val task: TaskWithSubtasks,
            ) : Snackbar(message)

            class TaskDeleted(val task: TaskWithSubtasks) : Snackbar(message = R.string.task_deleted)
        }
    }
}