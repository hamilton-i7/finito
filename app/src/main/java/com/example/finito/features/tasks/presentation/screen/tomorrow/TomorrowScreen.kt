package com.example.finito.features.tasks.presentation.screen.tomorrow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
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
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.core.presentation.util.calculateDp
import com.example.finito.core.presentation.util.menu.TomorrowScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.presentation.components.BoardsListSheetContent
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
                        appViewModel.onEvent(AppEvent.UndoTaskChange(task = event.task))
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
                    onTaskClick = { onNavigateToEditTask(it.task.taskId) },
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
                    }
                )
            }
            AnimatedVisibility(
                visible = tomorrowViewModel.selectedTask != null,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it }
            ) {
                TaskDateTimeFullDialog(
                    task = tomorrowViewModel.selectedTask ?: TaskWithSubtasks.dummyTasks.random(),
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
    onTaskClick: (TaskWithSubtasks) -> Unit = {},
    onPriorityClick: (TaskWithSubtasks) -> Unit = {},
    onDateTimeClick: (TaskWithSubtasks) -> Unit = {},
    onToggleTaskCompleted: (TaskWithSubtasks) -> Unit = {},
    onBoardNameClick: (TaskWithSubtasks) -> Unit = {},
) {
    val locale = LocalConfiguration.current.locales[0]
    val completedTasks = tasks.filterCompleted()
    val uncompletedTasks = tasks.filterUncompleted()

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

            items(
                items = uncompletedTasks,
                contentType = { ContentTypes.UNCOMPLETED_TASKS },
                key = { it.task.taskId }
            ) {
                TaskItem(
                    task = it.task,
                    boardName = boardNamesMap[it.task.boardId],
                    onTaskClick = { onTaskClick(it) },
                    onCompletedToggle = { onToggleTaskCompleted(it) },
                    onPriorityClick = { onPriorityClick(it) },
                    onBoardNameClick = { onBoardNameClick(it) },
                    onDateTimeClick = { onDateTimeClick(it) },
                    modifier = Modifier.animateItemPlacement()
                )
            }

            if (completedTasks.isEmpty()) return@LazyColumn

            item(key = LazyListKeys.SHOW_COMPLETED_TASKS_TOGGLE) {
                RowToggle(
                    showContent = showCompletedTasks,
                    onShowContentToggle = onToggleShowCompletedTasks,
                    label = stringResource(id = R.string.completed, completedTasks.size),
                    showContentDescription = R.string.show_completed_tasks,
                    hideContentDescription = R.string.hide_completed_tasks,
                    modifier = Modifier.animateItemPlacement()
                )
            }
            items(
                items = completedTasks,
                contentType = { ContentTypes.COMPLETED_TASKS },
                key = { it.task.taskId }
            ) {
                AnimatedVisibility(
                    visible = showCompletedTasks,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.animateItemPlacement()
                ) {
                    TaskItem(
                        task = it.task,
                        onCompletedToggle = { onToggleTaskCompleted(it) },
                        boardName = boardNamesMap[it.task.boardId],
                        onTaskClick = { onTaskClick(it) },
                    )
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