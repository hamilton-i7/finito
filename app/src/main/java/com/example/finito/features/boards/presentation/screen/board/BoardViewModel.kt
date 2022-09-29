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
import com.example.finito.features.subtasks.domain.entity.filterUncompleted
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

    private var recentlyReorderedSubtasks: List<Subtask> = emptyList()
    private var draggingItem: Int? = null

    var draggableTasks by mutableStateOf<List<Any>>(emptyList())
        private set

    private var currentDraggableTask: TaskWithSubtasks? = null

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
            is BoardEvent.ReorderTasks -> onReorder(event.to)
            BoardEvent.SaveTasksOrder -> onSaveTasksOrder()
            is BoardEvent.ReorderSubtasks -> TODO()
            BoardEvent.SaveSubtasksOrder -> TODO()
            is BoardEvent.DragItem -> draggingItem = event.itemId
        }
    }

    private fun onSaveTasksOrder() = viewModelScope.launch {
        currentDraggableTask = null
        val tasksIntersection = intersectTasksToSubtasks()
        val task = tasks.find { it.task.taskId == draggingItem }

        if (task != null && task.subtasks.isNotEmpty()) {
            with(draggableTasks.toMutableList()) {
                val newTaskId = filterIsInstance<Subtask>().find {
                    it.subtaskId == draggingItem
                }?.taskId
                val subtasks = tasks.flatMap {
                    it.subtasks
                }.filter { it.taskId == draggingItem }.also {
                    // 1. Remove related subtasks from current position
                    removeAll { if (it is Subtask) it.taskId == draggingItem else false }
                }.let {
                    if (newTaskId == null) return@let it
                    // 2. Change related subtasks' taskId to the new task
                    it.map { subtask -> subtask.copy(taskId = newTaskId) }
                }

                // 3. Add them next to their former related task
                val taskPosition = indexOfFirst {
                    if (it is Subtask) it.subtaskId == draggingItem
                    else (it as Task).taskId == draggingItem
                }
                if (taskPosition == lastIndex) {
                    addAll(subtasks)
                } else {
                    addAll(index = taskPosition + 1, elements = subtasks)
                }

                if (newTaskId != null) {
                    val subtask = first {
                        if (it is Subtask) it.subtaskId == tasksIntersection.first() else false
                    }
                    add(
                        index = indexOf(subtask),
                        element = (removeAt(indexOf(subtask)) as Subtask).copy(subtaskId = 0)
                    )
                }
                draggableTasks = this
            }
        }
//        if (task != null && task.subtasks.isNotEmpty()) {
//            val newTaskId = draggableTasks.filterIsInstance<Subtask>().first {
//                it.subtaskId == draggingItem
//            }.taskId
//            with(draggableTasks.toMutableList()) {
//                val subtasks = tasks.flatMap {
//                    it.subtasks
//                }.filter { it.taskId == draggingItem }.map {
//                    it.copy(taskId = newTaskId)
//                }.also { removeAll(it) }
//
//                val taskPosition = indexOf(tasks.first { it.task.taskId == draggingItem }.task)
//                if (taskPosition == lastIndex) {
//                    addAll(subtasks)
//                } else {
//                    addAll(index = taskPosition + 1, elements = subtasks)
//                }
//                draggableTasks = this
//                println("DRAGGABLE: $this")
//            }
//        }
        val tasks = mutableListOf<TaskWithSubtasks>().apply {
            draggableTasks.filterIsInstance<Task>().forEach {
                val subtasks = draggableTasks.filterIsInstance<Subtask>().filter { subtask ->
                    subtask.taskId == it.taskId
                }.toMutableList().apply subtasksApply@{
                    if (isEmpty()) return@subtasksApply
                    if (!wasTask()) return@subtasksApply

                    val subtask = find {
                            subtask -> subtask.subtaskId == draggingItem
                    } ?: return@subtasksApply
                    add(
                        index = indexOf(subtask),
                        element = removeAt(indexOf(subtask)).copy(subtaskId = 0)
                    )
                }
                add(
                    TaskWithSubtasks(
                        task = it.copy(
                            taskId = if (wasSubtask() && it.taskId == draggingItem) 0 else it.taskId
                        ),
                        subtasks = subtasks
                    )
                )
            }
        }
        when (taskUseCases.arrangeBoardTasks(tasks)) {
            is Result.Error -> {
                _eventFlow.emit(Event.ShowError(
                    error = R.string.arrange_tasks_error
                ))
            }
            is Result.Success -> {
                if (tasksIntersection.isNotEmpty()) {
                    val taskToDelete = this@BoardViewModel.tasks.first {
                        it.task.taskId == tasksIntersection.first()
                    }
                    taskUseCases.deleteTask(taskToDelete.task)
                    return@launch
                }
                if (wasSubtask()) {
                    val subtaskToDelete = this@BoardViewModel.tasks.flatMap { it.subtasks }.first {
                        it.subtaskId == intersectSubtasksToTasks().first()
                    }
                    subtaskUseCases.deleteSubtask(subtaskToDelete)
                }
            }
        }
    }

    private fun wasTask(): Boolean {
        val intersection = intersectTasksToSubtasks()
        if (intersection.isEmpty()) return false

        val task = tasks.find { it.task.taskId == intersection.first() }
        val subtask = draggableTasks.filterIsInstance<Subtask>().find {
            it.subtaskId == intersection.first()
        }
        return subtask != null && task != null
    }

    private fun wasSubtask(): Boolean {
        val intersection = intersectSubtasksToTasks()
        if (intersection.isEmpty()) return false

        val task = draggableTasks.filterIsInstance<Task>().find {
            it.taskId == intersection.first()
        }
        val subtask = tasks.flatMap { it.subtasks }.find {
            it.subtaskId == intersection.first()
        }
        return subtask != null && task != null
    }

    private fun intersectTasksToSubtasks(): Set<Int> {
        val allTasks = this@BoardViewModel.tasks.map { it.task.taskId }
        val subtasks = draggableTasks.filterIsInstance<Subtask>().map { it.subtaskId }
        return allTasks.intersect(subtasks.toSet())
    }

    private fun intersectSubtasksToTasks(): Set<Int> {
        val subtasks = this@BoardViewModel.tasks.flatMap { it.subtasks }.map { it.subtaskId }
        val allTasks = draggableTasks.filterIsInstance<Task>().map { it.taskId }
        return subtasks.intersect(allTasks.toSet())
    }

    fun canDrag(position: ItemPosition): Boolean {
        val tasks = getTasksWithNoCompletedSubtasks()
//        val subtasks = tasks.flatMap { it.subtasks }.filterUncompleted()

        tasks.find { it.task.taskId == draggingItem }?.let {
            if (it.subtasks.any { subtask -> subtask.subtaskId == position.key }) return false
        }
        return tasks.any {
            it.task.taskId == position.key
                    || it.subtasks.any { subtask -> subtask.subtaskId == position.key }
        }
//        val isTargetSubtask = subtasks.find { it.subtaskId == position.key } != null

//        if (draggingTask && isTargetSubtask) return false
//        if (draggingTask) return tasks.any { it.task.taskId == position.key }
//        println("POSITION KEY: ${position.key}")

//        return subtasks.any { it.subtaskId == position.key }
    }

    private fun onReorder(targetPosition: ItemPosition) {
        val fromTask = draggableTasks.filterIsInstance<Task>().find { it.taskId == draggingItem }
        val fromSubtask = draggableTasks.filterIsInstance<Subtask>().find {
            it.subtaskId == draggingItem
        }
        val targetTask = tasks.find { it.task.taskId == targetPosition.key }

        if (currentDraggableTask == null) {
            currentDraggableTask = tasks.find { it.task.taskId == draggingItem }
        }

        if (fromTask != null && targetTask != null) {
            val subtasks = draggableTasks.filterIsInstance<Subtask>().filter {
                it.taskId == fromTask.taskId
            }
            // From task to task
            if (subtasks.isEmpty() && targetTask.subtasks.isEmpty()) {
                reorderTasks(targetPosition)
                return
            }
            // From task with subtasks to task
            if (subtasks.isNotEmpty() && targetTask.subtasks.isEmpty()) {
                reorderTasks(targetPosition)
                return
            }
            reorderTaskToTaskWithSubtasks(targetPosition)
            return
//            val isTargetTaskWithSubtasks = tasks.find {
//                it.task.taskId == to.key
//            }?.subtasks?.isNotEmpty() ?: false

        }
        if (fromTask != null) {
            reorderTaskToTaskWithSubtasks(targetPosition)
            return
        }
        if (fromSubtask != null) {
            val subtask = draggableTasks.filterIsInstance<Subtask>().first { it.subtaskId == fromSubtask.subtaskId }
            val isLastSubtask = with(draggableTasks.filterIsInstance<Subtask>()) {
                find { it.subtaskId == targetPosition.key }?.let { foundSubtask ->
                    last { it.taskId == foundSubtask.taskId }.also { println("LAST SUBTASK: $it") }.subtaskId == foundSubtask.subtaskId
                }.also { println("NOT FOUND!!") } ?: false
            }
            val isRelatedTask = subtask.taskId == targetPosition.key
            if (isLastSubtask || isRelatedTask) {
                reorderSubtaskToOuterTask(targetPosition)
                return
            }
            if (targetTask != null) {
                if (targetTask.subtasks.isNotEmpty()) {
                    reorderSubtaskToTask(targetPosition)
                    return
                }
                reorderSubtaskToOuterTask(targetPosition)
                return
            }
            reorderTasks(targetPosition)
        }
//        reorderTasks(targetPosition)
//        println("RUNNING REORDER")
//        println("DRAGGING KEY: $draggingItem")
//        println("FROM KEY: ${from.key}")
//        println("TO KEY: ${to.key}")
//        val tasks: List<Task>
//        val subtasks: List<Subtask>
//        with(getTasksWithNoCompletedSubtasks()) {
//            tasks = map { it.task }
//            subtasks = flatMap { it.subtasks }
//        }
//        with(draggableTasks.toMutableList()) {
//            val draggingTask = tasks.find { it.taskId == draggingItem } != null
//            if (draggingTask) {
//                val newTaskPosition = indexOfFirst {
//                    if (it is Task) {
//                        it.taskId == to.key
//                    } else {
//                        (it as Subtask).subtaskId == to.key
//                    }
//                }
//                val oldTaskPosition = indexOfFirst {
//                    if (it is Task) it.taskId == draggingItem
//                    else (it as Subtask).subtaskId == draggingItem
//                }
//                add(
//                    index = newTaskPosition,
//                    element = removeAt(oldTaskPosition)
//                )
//                filter { it is Subtask && it.taskId == draggingItem }.forEachIndexed { index, _ ->
//                    println("NEW POSITION: $newTaskPosition")
//                    println("RESULT: ${newTaskPosition + index + 1}")
//                    add(
//                        index = newTaskPosition + index + 1,
//                        element = removeAt(index = oldTaskPosition + index + 1)
//                    )
//                }
//            }
//            draggableTasks = this
//            this@BoardViewModel.tasks = items.filterIsInstance<Task>().map { task ->
//                val relatedSubtasks = items.filterIsInstance<Subtask>().let {
//                    it.filter { subtask -> subtask.taskId == task.taskId }
//                }
//                TaskWithSubtasks(
//                    task = task,
//                    subtasks = relatedSubtasks
//                )
//            }
//        with(getTasksWithNoCompletedSubtasks()) {
//            val fromTask = indexOfFirst { it.task.taskId == draggingItem } != -1
//            val toTask = indexOfFirst { it.task.taskId == to.key } != -1
//
//            if (fromTask) {
//                // From task to task
//                reorderTasks(from, to, taskWithSubtasks = toMutableList())
//                return
//            }
//
//            // From subtask to task
//            if (toTask) {
//                reorderFromSubtaskToTask(from, to, taskWithSubtasks = toMutableList())
//                return
//            }
//            // From subtask to subtask
//            reorderSubtasks(from, to)
//        }
    }

    private fun reorderSubtaskToOuterTask(targetPosition: ItemPosition) {
        with(draggableTasks.toMutableList()) {
            add(
                index = indexOfFirst {
                    if (it is Subtask) it.subtaskId == targetPosition.key
                    else (it as Task).taskId == targetPosition.key
                },
                element = (removeAt(
                    indexOfFirst {
                        if (it is Subtask) it.subtaskId == draggingItem else false
                    }
                ) as Subtask).let {
                    currentDraggableTask?.task ?: Task(
                        taskId = draggingItem as Int,
                        boardId = board!!.board.boardId,
                        name = it.name,
                        description = it.description,
                        createdAt = it.createdAt
                    )
                }
            )
            draggableTasks = this
        }
    }

    private fun reorderSubtaskToTask(targetPosition: ItemPosition) {
        with(draggableTasks.toMutableList()) {
            add(
                index = indexOfFirst {
                    if (it is Task) it.taskId == targetPosition.key else false
                },
                element = (removeAt(
                    indexOfFirst {
                        if (it is Subtask) it.subtaskId == draggingItem else false
                    }
                ) as Subtask).copy(taskId = targetPosition.key as Int).also(::println)
            )
            draggableTasks = this
        }
    }

    private fun reorderTasks(targetPosition: ItemPosition) {
        with(draggableTasks.toMutableList()) {
            add(
                index = indexOfFirst {
                    if (it is Task) it.taskId == targetPosition.key
                    else (it as Subtask).subtaskId == targetPosition.key
                },
                element = removeAt(
                    indexOfFirst {
                        if (it is Task) it.taskId == draggingItem
                        else (it as Subtask).subtaskId == draggingItem
                    }
                )
            )
            draggableTasks = this
        }
//        val tasksWithNoCompletedSubtasks = taskWithSubtasks.apply {
//            add(
//                index = indexOfFirst { it.task.taskId == to.key },
//                element = removeAt(indexOfFirst { it.task.taskId == from.key })
//            )
//        }
//        val tasksWithCompletedSubtasks = tasks.filterCompleted().filter {
//            it.subtasks.filterCompleted().isNotEmpty()
//        }.map { it.copy(subtasks = it.subtasks.filterCompleted()) }
//        val completedTasks = tasks.filterCompleted()
//
//        tasks = tasksWithNoCompletedSubtasks + tasksWithCompletedSubtasks + completedTasks
    }

    private fun reorderTaskWithSubtasksToTask(to: ItemPosition) {
        with(draggableTasks.toMutableList()) {
            val task = tasks.first { it.task.taskId == draggingItem }
            val subtasks = filterIsInstance<Subtask>().filter { it.taskId == draggingItem }
//            removeAll(subtasks)
//            draggableTasks = this

            reorderTasks(to)

//            val newPosition = indexOfFirst {
//                if (it is Task) it.taskId == to.key
//                else (it as Subtask).subtaskId == to.key
//            }
//            currentDraggingTask = task
//
//            add(
//                index = newPosition,
//                element = removeAt(indexOf(task.task))
//            )
//            subtasks.forEachIndexed { index, subtask ->
//                add(
//                    index = newPosition + index + 1,
//                    element = subtask
//                )
//            }
//            draggableTasks = this
        }
    }

    private fun reorderTaskToTaskWithSubtasks(targetPosition: ItemPosition) {
        val targetItem = draggableTasks.find {
            if (it is Task) it.taskId == targetPosition.key
            else (it as Subtask).subtaskId == targetPosition.key
        }
        with(draggableTasks.toMutableList()) {
            val newPosition = indexOfFirst {
                if (it is Task) it.taskId == targetPosition.key
                else (it as Subtask).subtaskId == targetPosition.key
            }
            val element = removeAt(
                indexOfFirst {
                    if (it is Task) it.taskId == draggingItem
                    else (it as Subtask).subtaskId == draggingItem
                }
            ).let {
                if (it is Task) {
                    return@let Subtask(
                        subtaskId = draggingItem!!,
                        taskId = if (targetItem is Task) targetItem.taskId else (targetItem as Subtask).taskId,
                        name = it.name,
                        description = it.description,
                        createdAt = it.createdAt,
                    )
                }
                (it as Subtask).copy(
                    taskId = if (targetItem is Task) targetItem.taskId else (targetItem as Subtask).taskId,
                )
            }
            add(newPosition, element)
            draggableTasks = this
        }
    }

    private fun reorderFromSubtaskToTask(
        from: ItemPosition,
        to: ItemPosition,
        taskWithSubtasks: MutableList<TaskWithSubtasks>
    ) {
        val subtasks = taskWithSubtasks.flatMap { it.subtasks }.toMutableList().apply {
            removeAt(indexOfFirst { it.subtaskId == from.key })
        }
        val tasks = taskWithSubtasks.map { it.task }.toMutableList().apply {
            val task = first { it.taskId == to.key }
            val subtask = subtasks.first { it.subtaskId == from.key }

            add(
                index = indexOf(task),
                element = Task(
                    boardId = task.boardId,
                    name = subtask.name,
                    description = subtask.description,
                    createdAt = subtask.createdAt
                )
            )
        }
        this.tasks = tasks.map {
            TaskWithSubtasks(
                task = it,
                subtasks = subtasks.filter { subtask -> subtask.taskId == it.taskId }
            )
        }
    }

    private fun reorderSubtasks(from: ItemPosition, to: ItemPosition) {
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

    private fun getTasksWithNoCompletedSubtasks(): List<TaskWithSubtasks> {
        return tasks.filterUncompleted().map {
            it.copy(subtasks = it.subtasks.filterUncompleted())
        }
    }

    private fun setupDraggableTasks(): List<Any> {
        return mutableListOf<Any>().apply {
            getTasksWithNoCompletedSubtasks().forEach {
                add(it.task)
                it.subtasks.forEach { subtask ->  add(subtask) }
            }
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
                         draggableTasks = setupDraggableTasks()
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