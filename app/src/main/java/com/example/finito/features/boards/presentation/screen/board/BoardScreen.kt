package com.example.finito.features.boards.presentation.screen.board

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.core.presentation.util.menu.ActiveBoardScreenOption
import com.example.finito.core.presentation.util.menu.ArchivedBoardScreenMenuOption
import com.example.finito.core.presentation.util.menu.DeletedBoardScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.screen.board.components.BoardDialogs
import com.example.finito.features.boards.presentation.screen.board.components.BoardTopBar
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.filterCompleted
import com.example.finito.features.subtasks.presentation.components.SubtaskItem
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.presentation.components.CompletedTasksProgressBar
import com.example.finito.features.tasks.presentation.components.NewTaskSheetContent
import com.example.finito.features.tasks.presentation.components.TaskDateTimeFullDialog
import com.example.finito.features.tasks.presentation.components.TaskItem
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BoardScreen(
    drawerState: DrawerState,
    appViewModel: AppViewModel,
    showSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
    boardViewModel: BoardViewModel = hiltViewModel(),
    previousRoute: String? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateTask: (boardId: Int, name: String?) -> Unit = {_, _ -> },
    onNavigateToEditBoard: (boardId: Int, boardState: BoardState) -> Unit = {_, _ -> },
    onNavigateToEditTask: (taskId: Int) -> Unit = {},
) {
    val detailedBoard = boardViewModel.board

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current

    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val taskReorderableState = rememberReorderableLazyListState(
        onMove = { _, to ->
            boardViewModel.onEvent(BoardEvent.ReorderTasks(to))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        canDragOver = boardViewModel::canDrag,
        onDragEnd = onDragEnd@{ from, to ->
            if (from == to) return@onDragEnd
            boardViewModel.onEvent(BoardEvent.SaveTasksOrder)
        }
    )
    val subtaskReorderableState = rememberReorderableLazyListState(
        onMove = { _, to ->
            boardViewModel.onEvent(BoardEvent.ReorderTasks(to))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        canDragOver = boardViewModel::canDrag,
        onDragEnd = onDragEnd@{ from, to ->
            if (from == to) return@onDragEnd
            boardViewModel.onEvent(BoardEvent.SaveTasksOrder)
        }
    )
    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val expandedFab by remember {
//        derivedStateOf { reorderableState.listState.firstVisibleItemIndex == 0 }
        derivedStateOf { true }
    }
    val noCompletedTasks = boardViewModel.tasks.none { it.task.completed }
    val disabledMenuOptions = when (boardViewModel.boardState) {
        BoardState.ACTIVE -> listOf(ActiveBoardScreenOption.DeleteCompletedTasks)
        BoardState.ARCHIVED -> listOf(ArchivedBoardScreenMenuOption.DeleteCompletedTasks)
        BoardState.DELETED -> listOf(DeletedBoardScreenMenuOption.DeleteCompletedTasks)
    }

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        if (bottomSheetState.isVisible) {
            scope.launch { bottomSheetState.hide() }
            return@BackHandler
        }
        when (previousRoute) {
            Screen.Archive.route, Screen.Trash.route,
            Screen.Label.route, Screen.Home.route -> onNavigateBack()
            else -> onNavigateToHome()
        }
    }

    LaunchedEffect(Unit) {
        boardViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BoardViewModel.Event.Snackbar -> {
                    when (event) {
                        is BoardViewModel.Event.Snackbar.UndoBoardChange -> {
                            showSnackbar(event.message, R.string.undo) {
                                appViewModel.onEvent(AppEvent.UndoBoardChange(board = event.board))
                            }
                        }
                        is BoardViewModel.Event.Snackbar.UndoTaskCompletedToggle -> {
                            showSnackbar(event.message, R.string.undo) {
                                appViewModel.onEvent(AppEvent.UndoTaskCompletedToggle(task = event.task))
                            }
                        }
                    }
                }
                is BoardViewModel.Event.ShowError -> {
                    boardViewModel.onEvent(BoardEvent.ShowDialog(
                        type = BoardEvent.DialogType.Error(message = event.error)
                    ))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.eventFlow.collect { event ->
            if (event !is AppViewModel.Event.RefreshBoard) return@collect
            boardViewModel.onEvent(BoardEvent.RefreshBoard)
        }
    }

    LaunchedEffect(bottomSheetState.isVisible) {
        if (bottomSheetState.isVisible) return@LaunchedEffect
        focusManager.clearFocus()
        boardViewModel.onEvent(BoardEvent.ChangeNewTaskName(name = ""))
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetBackgroundColor = finitoColors.surface,
        sheetContent = {
            NewTaskSheetContent(
                nameTextFieldState = boardViewModel.newTaskNameState.copy(
                    onValueChange = {
                        boardViewModel.onEvent(BoardEvent.ChangeNewTaskName(it))
                    }),
                focusRequester = focusRequester,
                onViewMoreOptionsClick = {
                    scope.launch { bottomSheetState.hide() }
                    onNavigateToCreateTask(
                        detailedBoard!!.board.boardId,
                        boardViewModel.newTaskNameState.value.takeIf { it.isNotBlank() }
                    )
                },
                onSaveClick = {
                    boardViewModel.onEvent(BoardEvent.SaveTask)
                    scope.launch { bottomSheetState.hide() }
                },
                saveButtonEnabled = boardViewModel.newTaskNameState.value.isNotBlank()
            )
        },
    ) {
        Box {
            Scaffold(
                topBar = {
                    BoardTopBar(
                        onNavigationClick = onNavigationClick@{
                            if (detailedBoard?.board?.state != BoardState.ACTIVE) {
                                onNavigateBack()
                                return@onNavigationClick
                            }
                            scope.launch { drawerState.open() }
                        },
                        boardName = detailedBoard?.board?.name ?: "",
                        boardState = boardViewModel.boardState,
                        showMenu = boardViewModel.showScreenMenu,
                        onMoreOptionsClick = {
                            boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = true))
                        },
                        onDismissMenu = {
                            boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = false))
                        },
                        disabledOptions = if (noCompletedTasks) disabledMenuOptions else emptyList(),
                        onOptionClick = {
                            boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = false))

                            when (boardViewModel.boardState) {
                                BoardState.ARCHIVED -> {
                                    when (it as ArchivedBoardScreenMenuOption) {
                                        ArchivedBoardScreenMenuOption.DeleteBoard -> {
                                            boardViewModel.onEvent(BoardEvent.DeleteBoard)
                                            onNavigateBack()
                                        }
                                        ArchivedBoardScreenMenuOption.DeleteCompletedTasks -> {
                                            boardViewModel.onEvent(BoardEvent.ShowDialog(
                                                type = BoardEvent.DialogType.DeleteCompletedTasks
                                            ))
                                        }
                                        ArchivedBoardScreenMenuOption.EditBoard -> {
                                            onNavigateToEditBoard(
                                                detailedBoard!!.board.boardId,
                                                BoardState.ARCHIVED
                                            )
                                        }
                                        ArchivedBoardScreenMenuOption.UnarchiveBoard -> {
                                            boardViewModel.onEvent(BoardEvent.RestoreBoard)
                                            onNavigateBack()
                                        }
                                    }
                                }
                                BoardState.DELETED -> {
                                    when (it as DeletedBoardScreenMenuOption) {
                                        DeletedBoardScreenMenuOption.DeleteCompletedTasks -> {
                                            boardViewModel.onEvent(BoardEvent.ShowDialog(
                                                type = BoardEvent.DialogType.DeleteCompletedTasks
                                            ))
                                        }
                                        DeletedBoardScreenMenuOption.EditBoard -> {
                                            onNavigateToEditBoard(
                                                detailedBoard!!.board.boardId,
                                                BoardState.DELETED
                                            )
                                        }
                                        DeletedBoardScreenMenuOption.RestoreBoard -> {
                                            boardViewModel.onEvent(BoardEvent.RestoreBoard)
                                            onNavigateBack()
                                        }
                                    }
                                }
                                BoardState.ACTIVE -> {
                                    when (it as ActiveBoardScreenOption) {
                                        ActiveBoardScreenOption.ArchiveBoard -> {
                                            boardViewModel.onEvent(BoardEvent.ArchiveBoard)
                                            onNavigateToHome()
                                        }
                                        ActiveBoardScreenOption.DeleteBoard -> {
                                            boardViewModel.onEvent(BoardEvent.DeleteBoard)
                                            onNavigateToHome()
                                        }
                                        ActiveBoardScreenOption.DeleteCompletedTasks -> {
                                            boardViewModel.onEvent(BoardEvent.ShowDialog(
                                                type = BoardEvent.DialogType.DeleteCompletedTasks
                                            ))
                                        }
                                        ActiveBoardScreenOption.EditBoard -> {
                                            onNavigateToEditBoard(
                                                detailedBoard!!.board.boardId,
                                                BoardState.ACTIVE
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        scrollBehavior = topBarScrollBehavior
                    )
                },
                floatingActionButton = fab@{
                    if (boardViewModel.boardState == BoardState.DELETED) return@fab
                    CreateFab(
                        text = R.string.create_task,
                        onClick = {
                            scope.launch {
                                focusRequester.requestFocus()
                                bottomSheetState.show()
                            }
                        },
                        expanded = expandedFab
                    )
                },
                floatingActionButtonPosition = FabPosition.Center,
                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            ) { innerPadding ->
                BoardDialogs(boardViewModel)

                BoardScreen(
                    paddingValues = innerPadding,
                    hapticFeedback = hapticFeedback,
                    taskReorderableState = taskReorderableState,
                    subtaskReorderableState = subtaskReorderableState,
                    tasks = boardViewModel.tasks,
                    draggableTasks = boardViewModel.draggableTasks,
                    showCompletedTasks = boardViewModel.showCompletedTasks,
                    onToggleShowCompletedTasks = {
                        boardViewModel.onEvent(BoardEvent.ToggleCompletedTasksVisibility)
                    },
                    onTaskClick = { onNavigateToEditTask(it.taskId) },
                    onPriorityClick = {
                        boardViewModel.onEvent(BoardEvent.ShowDialog(
                            type = BoardEvent.DialogType.Priority(it)
                        ))
                    },
                    onDateTimeClick = {
                        boardViewModel.onEvent(BoardEvent.ShowTaskDateTimeFullDialog(it))
                    },
                    onToggleTaskCompleted = {
                        boardViewModel.onEvent(BoardEvent.ToggleTaskCompleted(it))
                    },
                    onDragging = {
                        boardViewModel.onEvent(BoardEvent.DragItem(it))
                    }
                )
            }

            AnimatedVisibility(
                visible = boardViewModel.selectedTask != null,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it }
            ) {
                TaskDateTimeFullDialog(
                    task = boardViewModel.selectedTask,
                    date = boardViewModel.selectedDate,
                    onDateFieldClick = {
                        boardViewModel.onEvent(BoardEvent.ShowDialog(
                            type = BoardEvent.DialogType.TaskDate
                        ))
                    },
                    onDateRemove = {
                        boardViewModel.onEvent(BoardEvent.ChangeTaskDate(date = null))
                        boardViewModel.onEvent(BoardEvent.ChangeTaskTime(time = null))
                    },
                    time = boardViewModel.selectedTime,
                    onTimeFieldClick = {
                        boardViewModel.onEvent(BoardEvent.ShowDialog(
                            type = BoardEvent.DialogType.TaskTime
                        ))
                    },
                    onTimeRemove = {
                        boardViewModel.onEvent(BoardEvent.ChangeTaskTime(time = null))
                    },
                    onAlertChangesMade = {
                        boardViewModel.onEvent(BoardEvent.ShowDialog(
                            type = BoardEvent.DialogType.DiscardChanges
                        ))
                    },
                    onCloseClick = {
                        boardViewModel.onEvent(BoardEvent.ShowTaskDateTimeFullDialog(task = null))
                    },
                    onSaveClick = {
                        boardViewModel.onEvent(BoardEvent.SaveTaskDateTimeChanges)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    taskReorderableState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    subtaskReorderableState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    tasks: List<TaskWithSubtasks> = emptyList(),
    draggableTasks: List<Any> = emptyList(),
    showCompletedTasks: Boolean = true,
    onToggleShowCompletedTasks: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onPriorityClick: (Task) -> Unit = {},
    onDateTimeClick: (Task) -> Unit = {},
    onToggleTaskCompleted: (TaskWithSubtasks) -> Unit = {},
    onSubtaskClick: (Subtask) -> Unit = {},
    onToggleSubtaskCompleted: (Subtask) -> Unit = {},
    onDragging: (Int?) -> Unit = {},
) {
    val uncompletedTasks = tasks.filterUncompleted()
    val tasksWithCompletedSubtasks = uncompletedTasks.filter {
        it.subtasks.filterCompleted().isNotEmpty()
    }.map { it.copy(subtasks = it.subtasks.filterCompleted()) }
    val completedTasks = tasks.filterCompleted()
    val totalTasksAmount = tasks.size + tasks.flatMap { it.subtasks }.size
    val completedTasksAmount = tasksWithCompletedSubtasks.flatMap { it.subtasks }.size
        .plus(completedTasks.flatMap { it.subtasks }.size)
        .plus(completedTasks.size)
    val draggingTask = tasks.find { it.task.taskId == taskReorderableState.draggingItemKey } != null
    var draggingContent by rememberSaveable { mutableStateOf<DragContent?>(null) }

    LaunchedEffect(taskReorderableState.draggingItemKey) {
        if (taskReorderableState.draggingItemKey == null) {
            draggingContent = null
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        onDragging(taskReorderableState.draggingItemKey as? Int)
    }

    LaunchedEffect(subtaskReorderableState.draggingItemKey) {
        if (subtaskReorderableState.draggingItemKey == null) {
            draggingContent = null
        }
    }

//    println("CONTENT: $draggingContent")

    // TODO 21/09/2022: Show board labels
    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        LazyColumn(
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
            state = taskReorderableState.listState,
            modifier = Modifier.reorderable(taskReorderableState),
        ) {
            item(
                key = LazyListKeys.COMPLETED_TASKS_PROGRESS_BAR,
                contentType = ContentTypes.PROGRESS_BAR
            ) {
                CompletedTasksProgressBar(
                    totalTasks = totalTasksAmount,
                    completedTasks = completedTasksAmount,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }

            items(
                items = draggableTasks,
                key = {
                    if (it is Task) it.taskId
                    else (it as Subtask).subtaskId
                }
            ) {
                if (it is Task) {
                    ReorderableItem(
                        reorderableState = taskReorderableState,
                        key = it.taskId,
                    ) { isDragging ->
                        TaskItem(
                            task = it,
                            hapticFeedback = hapticFeedback,
                            isDragging = isDragging,
                            onPriorityClick = { onPriorityClick(it) },
                            onCompletedToggle = {
                                val subtasks = draggableTasks.filterIsInstance<Subtask>().let { list ->
                                    list.filter { subtask -> subtask.taskId == it.taskId }
                                }
                                onToggleTaskCompleted(
                                    TaskWithSubtasks(
                                        task = it,
                                        subtasks = subtasks
                                    )
                                )
                            },
                            onTaskClick = { onTaskClick(it) },
                            onDateTimeClick = { onDateTimeClick(it) },
                            showDragIndicator = true,
                            modifier = Modifier
                                .animateItemPlacement()
                                .detectReorderAfterLongPress(taskReorderableState)
                        )
                    }
                } else if (it is Subtask && taskReorderableState.draggingItemKey != it.taskId) {
                    ReorderableItem(
                        reorderableState = taskReorderableState,
                        key = it.subtaskId,
                    ) { isDragging ->
                        SubtaskItem(
                            subtask = it,
                            isDragging = isDragging,
                            hapticFeedback = hapticFeedback,
                            showDragIndicator = true,
                            modifier = Modifier
                                .animateItemPlacement()
                                .detectReorderAfterLongPress(taskReorderableState)
                        )
                    }
                }
            }

//            draggableTasks.forEach { taskWithSubtasks ->
//                item(key = taskWithSubtasks.task.taskId) {
//                    ReorderableItem(
//                        reorderableState = taskReorderableState,
//                        key = taskWithSubtasks.task.taskId,
//                    ) { isDragging ->
//                        TaskItem(
//                            task = taskWithSubtasks.task,
//                            hapticFeedback = hapticFeedback,
//                            isDragging = isDragging,
//                            onPriorityClick = { onPriorityClick(taskWithSubtasks.task) },
//                            onCompletedToggle = { onToggleTaskCompleted(taskWithSubtasks) },
//                            onTaskClick = { onTaskClick(taskWithSubtasks.task) },
//                            onDateTimeClick = { onDateTimeClick(taskWithSubtasks.task) },
//                            showDragIndicator = true,
//                            modifier = Modifier
//                                .animateItemPlacement()
//                                .detectReorderAfterLongPress(taskReorderableState)
//                        )
//                    }
//                }
//                items(
//                    items = taskWithSubtasks.subtasks,
//                    key = { subtask -> subtask.subtaskId }
//                ) { subtask ->
//                    ReorderableItem(
//                        reorderableState = taskReorderableState,
//                        key = subtask.subtaskId,
//                    ) { isDragging ->
//                        SubtaskItem(
//                            subtask = subtask,
//                            isDragging = isDragging,
//                            hapticFeedback = hapticFeedback,
//                            showDragIndicator = true,
//                            modifier = Modifier
//                                .animateItemPlacement()
//                                .detectReorderAfterLongPress(taskReorderableState)
//                        )
//                    }
//                }
//            }
            if (completedTasksAmount != 0) {
                item(key = LazyListKeys.SHOW_COMPLETED_TASKS_TOGGLE) {
                    RowToggle(
                        showContent = showCompletedTasks,
                        onShowContentToggle = onToggleShowCompletedTasks,
                        label = stringResource(id = R.string.completed, completedTasksAmount),
                        showContentDescription = R.string.show_completed_tasks,
                        hideContentDescription = R.string.hide_completed_tasks,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                tasksWithCompletedSubtasks.forEach { (task, subtasks) ->
                    item(key = "${task.taskId} GHOST") {
                        AnimatedVisibility(
                            visible = showCompletedTasks,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            TaskItem(
                                task = task,
                                ghostVariant = true,
                                onTaskClick = { onTaskClick(task) },
                            )
                        }
                    }
                    items(
                        items = subtasks,
                        key = { "${it.subtaskId} GHOST COMPLETED" }
                    ) {
                        AnimatedVisibility(
                            visible = showCompletedTasks,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            SubtaskItem(
                                subtask = it,
                                onSubtaskClick = { onSubtaskClick(it) },
                                onCompletedToggle = { onToggleSubtaskCompleted(it) },
                            )
                        }
                    }
                }
                completedTasks.forEach {
                    item(key = "${it.task.taskId} COMPLETED") {
                        AnimatedVisibility(
                            visible = showCompletedTasks,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            TaskItem(
                                task = it.task,
                                onCompletedToggle = { onToggleTaskCompleted(it) },
                                onTaskClick = { onTaskClick(it.task) },
                            )
                        }
                    }
                    items(
                        items = it.subtasks,
                        key = { subtask -> "${subtask.subtaskId} COMPLETED" }
                    ) {
                        AnimatedVisibility(
                            visible = showCompletedTasks,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.animateItemPlacement()
                        ) {
                            SubtaskItem(
                                subtask = it,
                                onSubtaskClick = { onSubtaskClick(it) },
                                onCompletedToggle = { onToggleSubtaskCompleted(it) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun BoardScreenPreview() {
    FinitoTheme {
        Surface {
            BoardScreen(tasks = TaskWithSubtasks.dummyTasks.shuffled())
        }
    }
}

enum class DragContent {
    TASK, SUBTASK
}