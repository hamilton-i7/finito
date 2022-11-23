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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.EmptyContent
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.core.presentation.util.detectTapAndPressUnconsumed
import com.example.finito.core.presentation.util.menu.ActiveBoardScreenOption
import com.example.finito.core.presentation.util.menu.ArchivedBoardScreenMenuOption
import com.example.finito.core.presentation.util.menu.DeletedBoardScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.screen.board.components.BoardDialogs
import com.example.finito.features.boards.presentation.screen.board.components.BoardLabels
import com.example.finito.features.boards.presentation.screen.board.components.BoardTopBar
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelsListFullDialog
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
    onShowSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
    boardViewModel: BoardViewModel = hiltViewModel(),
    previousRoute: String? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateTask: (boardId: Int, name: String?) -> Unit = {_, _ -> },
    onNavigateToEditBoard: (boardId: Int, boardState: BoardState) -> Unit = { _, _ -> },
    onNavigateToEditTask: (taskId: Int) -> Unit = {},
    onNavigateToEditSubtask: (boardId: Int, subtaskId: Int) -> Unit = {_ , _ -> },
) {
    val detailedBoard = boardViewModel.board

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current

    val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val searchTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            boardViewModel.onEvent(BoardEvent.ReorderTasks(from, to))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        canDragOver = { draggedOver, _ -> boardViewModel.canDrag(draggedOver) },
        onDragEnd = { from, to ->
            boardViewModel.onEvent(BoardEvent.SaveTasksOrder(from, to))
        }
    )
    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val expandedFab by remember {
        derivedStateOf { reorderableState.listState.firstVisibleItemIndex == 0 }
    }
    val noCompletedTasks = boardViewModel.tasks.none { it.task.completed }
            && boardViewModel.tasks.flatMap { it.subtasks }.none { it.completed }
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
                        is BoardViewModel.Event.Snackbar.BoardStateChanged -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                appViewModel.onEvent(AppEvent.UndoBoardChange(board = event.board))
                            }
                        }
                        is BoardViewModel.Event.Snackbar.TaskCompletedStateChanged -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                appViewModel.onEvent(AppEvent.UndoTaskCompletedToggle(task = event.task))
                            }
                        }
                        is BoardViewModel.Event.Snackbar.SubtaskCompletedStateChanged -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                appViewModel.onEvent(AppEvent.UndoSubtaskCompletedToggle(
                                    subtask = event.subtask,
                                    task = event.task
                                ))
                            }
                        }
                        is BoardViewModel.Event.Snackbar.UneditableBoard -> {
                            onShowSnackbar(event.message, event.actionLabel) {
                                appViewModel.onEvent(AppEvent.RestoreUneditableBoard(event.board))
                            }
                        }
                    }
                }
                is BoardViewModel.Event.ShowError -> {
                    boardViewModel.onEvent(BoardEvent.ShowDialog(
                        type = BoardEvent.DialogType.Error(message = event.error)
                    ))
                }
                BoardViewModel.Event.NavigateBack -> onNavigateBack()
                BoardViewModel.Event.NavigateHome -> onNavigateToHome()
            }
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.event.collect { event ->
            when (event) {
                AppViewModel.Event.RefreshBoard -> {
                    boardViewModel.onEvent(BoardEvent.RefreshBoard)
                }
                is AppViewModel.Event.ShowError -> {
                    boardViewModel.onEvent(BoardEvent.ShowDialog(
                        type = BoardEvent.DialogType.Error(event.error)
                    ))
                }
                else -> Unit
            }
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
                                        }
                                        ArchivedBoardScreenMenuOption.DeleteCompletedTasks -> {
                                            boardViewModel.onEvent(BoardEvent.ShowDialog(
                                                type = BoardEvent.DialogType.DeleteCompletedTasks
                                            ))
                                        }
                                        ArchivedBoardScreenMenuOption.EditBoard -> {
                                            onNavigateToEditBoard(
                                                detailedBoard!!.board.boardId,
                                                BoardState.ARCHIVED,
                                            )
                                        }
                                        ArchivedBoardScreenMenuOption.UnarchiveBoard -> {
                                            boardViewModel.onEvent(BoardEvent.RestoreBoard)
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
                                                BoardState.DELETED,
                                            )
                                        }
                                        DeletedBoardScreenMenuOption.RestoreBoard -> {
                                            boardViewModel.onEvent(BoardEvent.RestoreBoard)
                                        }
                                    }
                                }
                                BoardState.ACTIVE -> {
                                    when (it as ActiveBoardScreenOption) {
                                        ActiveBoardScreenOption.ArchiveBoard -> {
                                            boardViewModel.onEvent(BoardEvent.ArchiveBoard)
                                        }
                                        ActiveBoardScreenOption.DeleteBoard -> {
                                            boardViewModel.onEvent(BoardEvent.DeleteBoard)
                                        }
                                        ActiveBoardScreenOption.DeleteCompletedTasks -> {
                                            boardViewModel.onEvent(BoardEvent.ShowDialog(
                                                type = BoardEvent.DialogType.DeleteCompletedTasks
                                            ))
                                        }
                                        ActiveBoardScreenOption.EditBoard -> {
                                            onNavigateToEditBoard(
                                                detailedBoard!!.board.boardId,
                                                BoardState.ACTIVE,
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
                modifier = Modifier
                    .nestedScroll(
                        if (boardViewModel.showLabelsFullDialog)
                            searchTopBarScrollBehavior.nestedScrollConnection
                        else
                            topBarScrollBehavior.nestedScrollConnection
                    )
            ) { innerPadding ->
                BoardDialogs(boardViewModel)

                BoardScreen(
                    paddingValues = innerPadding,
                    hapticFeedback = hapticFeedback,
                    reorderableState = reorderableState,
                    isDeleted = boardViewModel.boardState == BoardState.DELETED,
                    labels = boardViewModel.board?.labels ?: emptyList(),
                    tasks = boardViewModel.tasks,
                    draggableTasks = boardViewModel.draggableTasks,
                    showCompletedTasks = boardViewModel.showCompletedTasks,
                    onToggleShowCompletedTasks = {
                        boardViewModel.onEvent(BoardEvent.ToggleCompletedTasksVisibility)
                    },
                    onTaskClick = { onNavigateToEditTask(it.taskId) },
                    onSubtaskClick = {
                        onNavigateToEditSubtask(detailedBoard!!.board.boardId, it.subtaskId)
                    },
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
                    onToggleSubtaskCompleted = {
                        boardViewModel.onEvent(BoardEvent.ToggleSubtaskCompleted(it))
                    },
                    onDragging = {
                        boardViewModel.onEvent(BoardEvent.DragItem(it))
                    },
                    onLabelClick = {
                        boardViewModel.onEvent(BoardEvent.ShowLabelsFullDialog(show = true))
                    },
                    onScreenClick = {
                        boardViewModel.onEvent(BoardEvent.AlertNotEditable)
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

            AnimatedVisibility(
                visible = boardViewModel.showLabelsFullDialog,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it }
            ) {
                LabelsListFullDialog(
                    labels = boardViewModel.labels,
                    selectedLabels = boardViewModel.selectedLabels,
                    onLabelClick = {
                        boardViewModel.onEvent(BoardEvent.SelectLabel(it))
                    },
                    searchQuery = boardViewModel.labelSearchQuery.copy(
                        onValueChange = {
                            boardViewModel.onEvent(BoardEvent.SearchLabels(it))
                        }
                    ),
                    scrollBehavior = searchTopBarScrollBehavior,
                    onCloseClick = {
                        boardViewModel.onEvent(BoardEvent.ShowLabelsFullDialog(show = false))
                        boardViewModel.onEvent(BoardEvent.ChangeBoardLabels)
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
    reorderableState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    isDeleted: Boolean = false,
    labels: List<SimpleLabel> = emptyList(),
    onLabelClick: () -> Unit = {},
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
    onScreenClick: () -> Unit = {},
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

    LaunchedEffect(reorderableState.draggingItemKey) {
        if (reorderableState.draggingItemKey != null) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        onDragging(reorderableState.draggingItemKey as? Int)
    }

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .pointerInput(Unit) {
            detectTapAndPressUnconsumed(
                onTap = { if (isDeleted) onScreenClick() }
            )
        }
    ) {
        Crossfade(
            targetState = draggableTasks.isEmpty() && tasks.isEmpty()
        ) { isEmpty ->
            when (isEmpty) {
                true -> EmptyContent(
                    icon = R.drawable.todo_list,
                    title = R.string.no_tasks_title,
                    contentText = if (!isDeleted) R.string.no_tasks_content else null,
                    modifier = Modifier.padding(bottom = 120.dp),
                )
                false -> {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
                        state = reorderableState.listState,
                        modifier = Modifier.fillMaxSize().reorderable(reorderableState),
                    ) {
                        item(contentType = ContentTypes.PROGRESS_BAR) {
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
                            when (it) {
                                is Task -> {
                                    val subtasksAmount = tasks.flatMap {
                                            subtasks -> subtasks.subtasks
                                    }.count { subtask -> subtask.taskId == it.taskId && !subtask.completed }

                                    ReorderableItem(
                                        reorderableState = reorderableState,
                                        key = it.taskId,
                                    ) { isDragging ->
                                        TaskItem(
                                            task = it,
                                            subtasksAmount = subtasksAmount,
                                            enabled = !isDeleted,
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
                                            showDragIndicator = !isDeleted,
                                            modifier = if (isDeleted) Modifier
                                            else Modifier.detectReorderAfterLongPress(reorderableState)
                                        )
                                    }
                                }
                                is Subtask -> {
                                    val draggingSubtask = reorderableState.draggingItemKey == it.subtaskId

                                    AnimatedVisibility(
                                        visible = reorderableState.draggingItemKey != it.taskId,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        modifier = Modifier
                                            .zIndex(if (draggingSubtask) 1f else -1f)
                                            .then(
                                                // Remove duplicate animation while dragging among subtasks
                                                other = if (!draggingSubtask)
                                                    Modifier.animateItemPlacement()
                                                else
                                                    Modifier
                                            )
                                    ) {
                                        ReorderableItem(
                                            state = reorderableState,
                                            key = it.subtaskId,
                                        ) { isDragging ->
                                            SubtaskItem(
                                                subtask = it,
                                                isDragging = isDragging,
                                                enabled = !isDeleted,
                                                showDragIndicator = true,
                                                onSubtaskClick = { onSubtaskClick(it) },
                                                onCompletedToggle = { onToggleSubtaskCompleted(it) },
                                                modifier = Modifier.detectReorderAfterLongPress(reorderableState)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (completedTasksAmount != 0) {
                            item(key = LazyListKeys.SHOW_COMPLETED_TASKS_TOGGLE) {
                                RowToggle(
                                    showContent = showCompletedTasks,
                                    onShowContentToggle = onToggleShowCompletedTasks,
                                    label = stringResource(id = R.string.completed, completedTasksAmount),
                                    showContentDescription = R.string.show_completed_tasks,
                                    hideContentDescription = R.string.hide_completed_tasks,
                                    modifier = Modifier.animateItemPlacement(),
                                    enabled = !isDeleted,
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
                                            enabled = false,
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
                                            enabled = !isDeleted,
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
                                            enabled = !isDeleted,
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
                                            enabled = !isDeleted,
                                            onSubtaskClick = { onSubtaskClick(it) },
                                            onCompletedToggle = { onToggleSubtaskCompleted(it) },
                                        )
                                    }
                                }
                            }
                        }
                        item(contentType = ContentTypes.LABELS) {
                            BoardLabels(
                                labels = labels.sortedBy { it.normalizedName },
                                onLabelClick = onLabelClick,
                                enabled = !isDeleted,
                                modifier = Modifier.padding(horizontal = 16.dp)
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