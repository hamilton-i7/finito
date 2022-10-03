package com.example.finito.features.tasks.presentation.screen.tomorrow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.domain.util.prioritySortingOptions
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.components.SortingChips
import com.example.finito.core.presentation.components.bars.SmallTopBarWithMenu
import com.example.finito.core.presentation.util.AnimationDurationConstants
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.core.presentation.util.calculateDp
import com.example.finito.core.presentation.util.menu.TomorrowScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.presentation.components.BoardsListSheetContent
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.filterCompleted
import com.example.finito.features.subtasks.domain.entity.filterUncompleted
import com.example.finito.features.subtasks.presentation.components.SubtaskItem
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.domain.util.toFullFormat
import com.example.finito.features.tasks.presentation.components.NewTaskSheetContent
import com.example.finito.features.tasks.presentation.components.TaskDateTimeFullDialog
import com.example.finito.features.tasks.presentation.components.TaskItem
import com.example.finito.features.tasks.presentation.screen.tomorrow.components.TomorrowDialogs
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TomorrowScreen(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    tomorrowViewModel: TomorrowViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onNavigateToCreateTask: (boardId: Int, name: String?) -> Unit = {_, _ -> },
    onNavigateToEditTask: (taskId: Int) -> Unit = {},
    onNavigateToEditSubtask: (boardId: Int, subtaskId: Int) -> Unit = {_ , _ -> },
    finishActivity: () -> Unit = {},
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit,
    ) -> Unit = {_, _, _ -> },
) {
    val listState = rememberLazyListState()
    val bottomSheetListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val bottomSheetCorners by animateDpAsState(
        targetValue = calculateDp(bottomSheetState)
    )
    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    var creatingTask by rememberSaveable { mutableStateOf(false) }
    val noCompletedTasks = tomorrowViewModel.tasks.filterCompleted().isEmpty()
    val disabledMenuOptions = listOf(TomorrowScreenMenuOption.DeleteCompleted)

    BackHandler {
        if (creatingTask
            && tomorrowViewModel.bottomSheetContent is TomorrowEvent.BottomSheetContent.BoardsList) {
            tomorrowViewModel.onEvent(TomorrowEvent.ChangeBottomSheetContent(
                TomorrowEvent.BottomSheetContent.NewTask
            ))
            return@BackHandler
        }
        if (bottomSheetState.isVisible) {
            scope.launch { bottomSheetState.hide() }
            creatingTask = false
            return@BackHandler
        }
        finishActivity()
    }

    LaunchedEffect(Unit) {
        tomorrowViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is TomorrowViewModel.Event.ShowError -> {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog(
                        type = TomorrowEvent.DialogType.Error(message = event.error)
                    ))
                }
                is TomorrowViewModel.Event.Snackbar.UndoTaskChange -> {
                    onShowSnackbar(event.message, R.string.undo) {
                        appViewModel.onEvent(AppEvent.UndoTaskCompletedToggle(task = event.task))
                    }
                }
                is TomorrowViewModel.Event.Snackbar.UndoSubtaskCompletedToggle -> {
                    onShowSnackbar(event.message, R.string.undo) {
                        appViewModel.onEvent(AppEvent.UndoSubtaskCompletedToggle(subtask = event.subtask))
                    }
                }
            }
        }
    }

    LaunchedEffect(bottomSheetState.currentValue) {
        if (bottomSheetState.currentValue != ModalBottomSheetValue.Hidden) return@LaunchedEffect

        if (tomorrowViewModel.bottomSheetContent == TomorrowEvent.BottomSheetContent.NewTask) {
            // Reset flag to initial state
            creatingTask = false
        }
    }

    LaunchedEffect(bottomSheetState.isVisible) {
        if (bottomSheetState.isVisible) return@LaunchedEffect
        focusManager.clearFocus()
        tomorrowViewModel.onEvent(TomorrowEvent.DismissBottomSheet)
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = when (tomorrowViewModel.bottomSheetContent) {
            is TomorrowEvent.BottomSheetContent.BoardsList -> RoundedCornerShape(
                topStart = bottomSheetCorners,
                topEnd = bottomSheetCorners
            )
            TomorrowEvent.BottomSheetContent.NewTask -> RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            )
        },
        sheetBackgroundColor = finitoColors.surface,
        sheetContent = {
            when (tomorrowViewModel.bottomSheetContent) {
                is TomorrowEvent.BottomSheetContent.BoardsList -> {
                    BoardsListSheetContent(
                        state = bottomSheetListState,
                        boards = tomorrowViewModel.boards,
                        selectedBoard = tomorrowViewModel.selectedBoard,
                        onBoardClick = {
                            val task = (
                                    tomorrowViewModel.bottomSheetContent as TomorrowEvent
                                    .BottomSheetContent
                                    .BoardsList
                                    ).task
                            if (creatingTask) {
                                tomorrowViewModel.onEvent(TomorrowEvent.ChangeBottomSheetContent(
                                    TomorrowEvent.BottomSheetContent.NewTask
                                ))
                            } else {
                                scope.launch { bottomSheetState.hide() }
                            }
                            tomorrowViewModel.onEvent(TomorrowEvent.ChangeBoard(board = it, task = task))
                        }
                    )
                }
                TomorrowEvent.BottomSheetContent.NewTask -> {
                    NewTaskSheetContent(
                        nameTextFieldState = tomorrowViewModel.newTaskNameState.copy(
                            onValueChange = {
                                tomorrowViewModel.onEvent(TomorrowEvent.ChangeNewTaskName(it))
                            }),
                        focusRequester = focusRequester,
                        onViewMoreOptionsClick = {
                            scope.launch { bottomSheetState.hide() }
                            onNavigateToCreateTask(
                                tomorrowViewModel.selectedBoard!!.boardId,
                                tomorrowViewModel.newTaskNameState.value.takeIf { it.isNotBlank() }
                            )
                        },
                        onSaveClick = {
                            tomorrowViewModel.onEvent(TomorrowEvent.SaveNewTask)
                            scope.launch { bottomSheetState.hide() }
                        },
                        saveButtonEnabled = tomorrowViewModel.newTaskNameState.value.isNotBlank(),
                        includeBoardIndicator = true,
                        selectedBoardName = tomorrowViewModel.selectedBoard?.name ?: "",
                        boardsMenuExpanded = bottomSheetState.isVisible,
                        onBoardIndicatorClick = onBoardIndicatorClick@{
                            if (tomorrowViewModel.selectedBoard == null) return@onBoardIndicatorClick
                            scope.launch {
                                bottomSheetListState.scrollToItem(
                                    index = tomorrowViewModel.boards.indexOf(tomorrowViewModel.selectedBoard)
                                )
                            }
                            tomorrowViewModel.onEvent(TomorrowEvent.ChangeBottomSheetContent(
                                content = TomorrowEvent.BottomSheetContent.BoardsList()
                            ))
                        }
                    )
                }
            }
        }
    ) {
        Box {
            Scaffold(
                topBar = {
                    SmallTopBarWithMenu(
                        title = stringResource(id = R.string.tomorrow),
                        showMenu = tomorrowViewModel.showScreenMenu,
                        onDismissMenu = {
                            tomorrowViewModel.onEvent(TomorrowEvent.ShowScreenMenu(show = false))
                        },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onMoreOptionsClick = {
                            tomorrowViewModel.onEvent(TomorrowEvent.ShowScreenMenu(show = true))
                        },
                        options = listOf<TomorrowScreenMenuOption>(
                            TomorrowScreenMenuOption.DeleteCompleted
                        ),
                        disabledOptions = if (noCompletedTasks) disabledMenuOptions else emptyList(),
                        onOptionClick = { option ->
                            tomorrowViewModel.onEvent(TomorrowEvent.ShowScreenMenu(show = false))
                            when (option) {
                                TomorrowScreenMenuOption.DeleteCompleted -> {
                                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog(
                                        type = TomorrowEvent.DialogType.DeleteCompleted
                                    ))
                                }
                            }
                        },
                        scrollBehavior = topBarScrollBehavior,
                    )
                },
                floatingActionButton = {
                    CreateFab(
                        text = R.string.create_task,
                        onClick = {
                            creatingTask = true
                            tomorrowViewModel.onEvent(TomorrowEvent.ChangeBottomSheetContent(
                                TomorrowEvent.BottomSheetContent.NewTask
                            ))
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
                TomorrowDialogs(tomorrowViewModel)

                TomorrowScreen(
                    paddingValues = innerPadding,
                    listState = listState,
                    selectedSortingOption = tomorrowViewModel.sortingOption,
                    onSortingOptionClick = onSortingOptionClick@{
                        if (tomorrowViewModel.sortingOption == it) {
                            tomorrowViewModel.onEvent(TomorrowEvent.SortByPriority(option =  null))
                            return@onSortingOptionClick
                        }
                        tomorrowViewModel.onEvent(TomorrowEvent.SortByPriority(it))
                    },
                    boardNamesMap = tomorrowViewModel.boardNamesMap,
                    tasks = tomorrowViewModel.tasks,
                    showCompletedTasks = tomorrowViewModel.showCompletedTasks,
                    onToggleShowCompletedTasks = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ToggleCompletedTasksVisibility)
                    },
                    onTaskClick = { onNavigateToEditTask(it.taskId) },
                    onPriorityClick = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog(
                            type = TomorrowEvent.DialogType.Priority(it)
                        ))
                    },
                    onDateTimeClick = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ShowTaskDateTimeFullDialog(it))
                    },
                    onToggleTaskCompleted = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ToggleTaskCompleted(it))
                    },
                    onBoardNameClick = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ChangeBottomSheetContent(
                            TomorrowEvent.BottomSheetContent.BoardsList(it)
                        ))
                        scope.launch {
                            if (tomorrowViewModel.selectedBoard == null) return@launch
                            bottomSheetListState.scrollToItem(
                                index = tomorrowViewModel.boards.indexOf(tomorrowViewModel.selectedBoard)
                            )
                            bottomSheetState.show()
                        }
                    },
                    onSubtaskClick = {
                        onNavigateToEditSubtask(tomorrowViewModel.selectedBoard!!.boardId, it.subtaskId)
                    },
                    onToggleSubtaskCompleted = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ToggleSubtaskCompleted(it))
                    },
                )
            }
            AnimatedVisibility(
                visible = tomorrowViewModel.selectedTask != null,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it }
            ) {
                TaskDateTimeFullDialog(
                    task = tomorrowViewModel.selectedTask,
                    date = tomorrowViewModel.selectedDate,
                    onDateFieldClick = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog(
                            type = TomorrowEvent.DialogType.TaskDate
                        ))
                    },
                    onDateRemove = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ChangeDate(date = null))
                        tomorrowViewModel.onEvent(TomorrowEvent.ChangeTime(time = null))
                    },
                    time = tomorrowViewModel.selectedTime,
                    onTimeFieldClick = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog(
                            type = TomorrowEvent.DialogType.TaskTime
                        ))
                    },
                    onTimeRemove = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ChangeTime(time = null))
                    },
                    onAlertChangesMade = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog(
                            type = TomorrowEvent.DialogType.DiscardChanges
                        ))
                    },
                    onCloseClick = {
                        tomorrowViewModel.onEvent(TomorrowEvent.ShowTaskDateTimeFullDialog(task = null))
                    },
                    onSaveClick = {
                        tomorrowViewModel.onEvent(TomorrowEvent.SaveTaskDateTimeChanges)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TomorrowScreen(
    paddingValues: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
    selectedSortingOption: SortingOption.Priority? = null,
    onSortingOptionClick: (SortingOption.Priority) -> Unit = {},
    boardNamesMap: Map<Int, String> = mapOf(),
    tasks: List<TaskWithSubtasks> = emptyList(),
    showCompletedTasks: Boolean = true,
    onToggleShowCompletedTasks: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onPriorityClick: (Task) -> Unit = {},
    onDateTimeClick: (Task) -> Unit = {},
    onToggleTaskCompleted: (TaskWithSubtasks) -> Unit = {},
    onBoardNameClick: (Task) -> Unit = {},
    onSubtaskClick: (Subtask) -> Unit = {},
    onToggleSubtaskCompleted: (Subtask) -> Unit = {},
) {
    val locale = LocalConfiguration.current.locales[0]
    val uncompletedTasks = tasks.filterUncompleted()

    val tasksWithNoCompletedSubtasks = uncompletedTasks.map {
        it.copy(subtasks = it.subtasks.filterUncompleted())
    }
    val tasksWithCompletedSubtasks = uncompletedTasks.filter {
        it.subtasks.filterCompleted().isNotEmpty()
    }.map { it.copy(subtasks = it.subtasks.filterCompleted()) }

    val completedTasks = tasks.filterCompleted()
    val completedTasksAmount = tasksWithCompletedSubtasks.flatMap { it.subtasks }.size
        .plus(completedTasks.flatMap { it.subtasks }.size)
        .plus(completedTasks.size)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
            state = listState
        ) {
            item {
                SortingChips(
                    options = prioritySortingOptions,
                    selectedOption = selectedSortingOption,
                    onOptionClick = onSortingOptionClick,
                    modifier = Modifier.padding(
                        bottom = 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                )
            }
            item {
                Text(
                    text = LocalDate.now().plusDays(1).toFullFormat(locale, complete = true),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        bottom = 12.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                )
            }

            tasksWithNoCompletedSubtasks.forEach { (task, subtasks) ->
                item(key = task.taskId) {
                    TaskItem(
                        task = task,
                        boardName = boardNamesMap[task.boardId],
                        onTaskClick = { onTaskClick(task) },
                        onCompletedToggle = {
                            onToggleTaskCompleted(TaskWithSubtasks(task, subtasks))
                        },
                        onPriorityClick = { onPriorityClick(task) },
                        onBoardNameClick = { onBoardNameClick(task) },
                        onDateTimeClick = { onDateTimeClick(task) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                items(
                    items = subtasks,
                    key = { it.subtaskId }
                ) {
                    SubtaskItem(
                        subtask = it,
                        onSubtaskClick = { onSubtaskClick(it) },
                        onCompletedToggle = { onToggleSubtaskCompleted(it) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }

            if (completedTasksAmount == 0) return@LazyColumn

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
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationDurationConstants.LongDurationMillis,
                                delayMillis = AnimationDurationConstants.ShortestDurationMillis
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = AnimationDurationConstants.RegularDurationMillis)
                        ),
                        modifier = Modifier.animateItemPlacement()
                    ) {
                        TaskItem(
                            task = task,
                            boardName = boardNamesMap[task.boardId],
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
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationDurationConstants.LongDurationMillis,
                                delayMillis = AnimationDurationConstants.ShortestDurationMillis
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = AnimationDurationConstants.RegularDurationMillis)
                        ),
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
            completedTasks.forEach { (task, subtasks) ->
                item(key = "${task.taskId} COMPLETED") {
                    AnimatedVisibility(
                        visible = showCompletedTasks,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationDurationConstants.LongDurationMillis,
                                delayMillis = AnimationDurationConstants.ShortestDurationMillis
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = AnimationDurationConstants.RegularDurationMillis)
                        ),
                        modifier = Modifier.animateItemPlacement()
                    ) {
                        TaskItem(
                            task = task,
                            onCompletedToggle = {
                                onToggleTaskCompleted(TaskWithSubtasks(task, subtasks))
                            },
                            onTaskClick = { onTaskClick(task) },
                        )
                    }
                }
                items(
                    items = subtasks,
                    key = { subtask -> "${subtask.subtaskId} COMPLETED" }
                ) {
                    AnimatedVisibility(
                        visible = showCompletedTasks,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationDurationConstants.LongDurationMillis,
                                delayMillis = AnimationDurationConstants.ShortestDurationMillis
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = AnimationDurationConstants.RegularDurationMillis)
                        ),
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

@CompletePreviews
@Composable
private fun TomorrowScreenPreview() {
    FinitoTheme {
        Surface {
            TomorrowScreen(tasks = TaskWithSubtasks.dummyTasks)
        }
    }
}