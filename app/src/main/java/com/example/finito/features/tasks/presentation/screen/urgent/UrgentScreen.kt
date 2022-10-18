package com.example.finito.features.tasks.presentation.screen.urgent

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.bars.SmallTopBarWithMenu
import com.example.finito.core.presentation.util.AnimationDurationConstants.LongDurationMillis
import com.example.finito.core.presentation.util.AnimationDurationConstants.RegularDurationMillis
import com.example.finito.core.presentation.util.calculateDp
import com.example.finito.core.presentation.util.menu.UrgentScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.presentation.components.BoardsListSheetContent
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.presentation.components.NewTaskSheetContent
import com.example.finito.features.tasks.presentation.components.TaskDateTimeFullDialog
import com.example.finito.features.tasks.presentation.screen.urgent.components.UrgentDialogs
import com.example.finito.features.tasks.presentation.screen.urgent.components.UrgentTabs
import com.example.finito.features.tasks.presentation.screen.urgent.components.UrgentTabsContent
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun UrgentScreen(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    urgentViewModel: UrgentViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onNavigateToCreateTask: (boardId: Int, name: String?) -> Unit = {_, _ -> },
    onNavigateToEditTask: (taskId: Int) -> Unit = {},
    onNavigateToEditSubtask: (boardId: Int, subtaskId: Int) -> Unit = {_ , _ -> },
    onNavigateToCreateBoard: () -> Unit = {},
    finishActivity: () -> Unit = {},
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit,
    ) -> Unit = {_, _, _ -> },
) {
    val pagerState = rememberPagerState()
    val listStates = listOf(
        rememberLazyListState(),
        rememberLazyListState(),
    )
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
        // Control FAB expanded state per Tab
        derivedStateOf {
            when (pagerState.currentPage) {
                0 -> listStates[0].firstVisibleItemIndex == 0
                else -> listStates[1].firstVisibleItemIndex == 0
            }
        }
    }
    var creatingTask by rememberSaveable { mutableStateOf(false) }
    val noCompletedTasks = urgentViewModel.tasks.values.flatten().none { it.task.completed }
            && urgentViewModel.tasks.values.flatten().flatMap { it.subtasks }.none { it.completed }
    val disabledMenuOptions = listOf(UrgentScreenMenuOption.DeleteCompleted)

    BackHandler {
        if (creatingTask
            && urgentViewModel.bottomSheetContent is UrgentEvent.BottomSheetContent.BoardsList
        ) {
            urgentViewModel.onEvent(UrgentEvent.ChangeBottomSheetContent(
                UrgentEvent.BottomSheetContent.NewTask
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
        urgentViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UrgentViewModel.Event.ShowError -> {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog(
                        type = UrgentEvent.DialogType.Error(message = event.error)
                    ))
                }
                is UrgentViewModel.Event.Snackbar.UndoTaskChange -> {
                    onShowSnackbar(event.message, R.string.undo) {
                        appViewModel.onEvent(AppEvent.UndoTaskCompletedToggle(task = event.task))
                    }
                }
                is UrgentViewModel.Event.Snackbar.UndoSubtaskCompletedToggle -> {
                    onShowSnackbar(event.message, R.string.undo) {
                        appViewModel.onEvent(AppEvent.UndoSubtaskCompletedToggle(
                            subtask = event.subtask,
                            task = event.task
                        ))
                    }
                }
                UrgentViewModel.Event.NavigateToCreateBoard -> onNavigateToCreateBoard()
            }
        }
    }

    LaunchedEffect(bottomSheetState.currentValue) {
        if (bottomSheetState.currentValue != ModalBottomSheetValue.Hidden) return@LaunchedEffect

        if (urgentViewModel.bottomSheetContent == UrgentEvent.BottomSheetContent.NewTask) {
            // Reset flag to initial state
            creatingTask = false
        }
    }

    LaunchedEffect(bottomSheetState.isVisible) {
        if (bottomSheetState.isVisible) return@LaunchedEffect
        focusManager.clearFocus()
        urgentViewModel.onEvent(UrgentEvent.DismissBottomSheet)
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = when (urgentViewModel.bottomSheetContent) {
            is UrgentEvent.BottomSheetContent.BoardsList -> RoundedCornerShape(
                topStart = bottomSheetCorners,
                topEnd = bottomSheetCorners
            )
            UrgentEvent.BottomSheetContent.NewTask -> RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            )
        },
        sheetBackgroundColor = finitoColors.surface,
        sheetContent = {
            when (urgentViewModel.bottomSheetContent) {
                is UrgentEvent.BottomSheetContent.BoardsList -> {
                    BoardsListSheetContent(
                        state = bottomSheetListState,
                        boards = urgentViewModel.boards,
                        selectedBoard = urgentViewModel.selectedBoard,
                        onBoardClick = {
                            val task = (
                                    urgentViewModel.bottomSheetContent as UrgentEvent.BottomSheetContent.BoardsList
                                    ).task
                            if (creatingTask) {
                                urgentViewModel.onEvent(UrgentEvent.ChangeBottomSheetContent(
                                    UrgentEvent.BottomSheetContent.NewTask
                                ))
                            } else {
                                scope.launch { bottomSheetState.hide() }
                            }
                            urgentViewModel.onEvent(UrgentEvent.ChangeBoard(board = it,
                                task = task))
                        }
                    )
                }
                UrgentEvent.BottomSheetContent.NewTask -> {
                    NewTaskSheetContent(
                        nameTextFieldState = urgentViewModel.newTaskNameState.copy(
                            onValueChange = {
                                urgentViewModel.onEvent(UrgentEvent.ChangeNewTaskName(it))
                            }),
                        focusRequester = focusRequester,
                        onViewMoreOptionsClick = {
                            scope.launch { bottomSheetState.hide() }
                            onNavigateToCreateTask(
                                urgentViewModel.selectedBoard!!.boardId,
                                urgentViewModel.newTaskNameState.value.takeIf { it.isNotBlank() }
                            )
                        },
                        onSaveClick = {
                            urgentViewModel.onEvent(UrgentEvent.SaveNewTask)
                            scope.launch { bottomSheetState.hide() }
                        },
                        saveButtonEnabled = urgentViewModel.newTaskNameState.value.isNotBlank(),
                        includeBoardIndicator = true,
                        selectedBoardName = urgentViewModel.selectedBoard?.name ?: "",
                        boardsMenuExpanded = bottomSheetState.isVisible,
                        onBoardIndicatorClick = onBoardIndicatorClick@{
                            if (urgentViewModel.selectedBoard == null) return@onBoardIndicatorClick
                            scope.launch {
                                bottomSheetListState.scrollToItem(
                                    index = urgentViewModel.boards.indexOf(urgentViewModel.selectedBoard)
                                )
                            }
                            urgentViewModel.onEvent(UrgentEvent.ChangeBottomSheetContent(
                                content = UrgentEvent.BottomSheetContent.BoardsList()
                            ))
                        }
                    )
                }
            }
        }
    ) {
        Box {
            Scaffold(
                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                topBar = {
                    SmallTopBarWithMenu(
                        title = stringResource(id = R.string.urgent),
                        showMenu = urgentViewModel.showScreenMenu,
                        onDismissMenu = {
                            urgentViewModel.onEvent(UrgentEvent.ShowScreenMenu(show = false))
                        },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onMoreOptionsClick = {
                            urgentViewModel.onEvent(UrgentEvent.ShowScreenMenu(show = true))
                        },
                        options = listOf<UrgentScreenMenuOption>(
                            UrgentScreenMenuOption.DeleteCompleted
                        ),
                        disabledOptions = if (noCompletedTasks) disabledMenuOptions else emptyList(),
                        onOptionClick = { option ->
                            urgentViewModel.onEvent(UrgentEvent.ShowScreenMenu(show = false))
                            when (option) {
                                UrgentScreenMenuOption.DeleteCompleted -> {
                                    urgentViewModel.onEvent(UrgentEvent.ShowDialog(
                                        type = UrgentEvent.DialogType.DeleteCompleted
                                    ))
                                }
                            }
                        },
                        scrollBehavior = topBarScrollBehavior,
                    )
                },
                floatingActionButton = {
                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            scaleIn(
                                initialScale = 0f,
                                transformOrigin = TransformOrigin(
                                    pivotFractionX = 0.5f,
                                    pivotFractionY = 0.60f
                                ),
                                animationSpec = tween(
                                    delayMillis = RegularDurationMillis,
                                    durationMillis = LongDurationMillis
                                )
                            ) with scaleOut(
                                targetScale = 0f,
                                transformOrigin = TransformOrigin(
                                    pivotFractionX = 0.5f,
                                    pivotFractionY = 0.60f
                                ),
                                animationSpec = tween(
                                    durationMillis = RegularDurationMillis
                                )
                            )
                        }
                    ) {
                        CreateFab(
                            text = R.string.create_task,
                            onClick = onClick@{
                                if (urgentViewModel.boards.isEmpty()) {
                                    urgentViewModel.onEvent(UrgentEvent.ShowDialog(
                                        type = UrgentEvent.DialogType.CreateBoard
                                    ))
                                    return@onClick
                                }
                                creatingTask = true
                                urgentViewModel.onEvent(UrgentEvent.ChangeBottomSheetContent(
                                    UrgentEvent.BottomSheetContent.NewTask
                                ))
                                scope.launch {
                                    focusRequester.requestFocus()
                                    bottomSheetState.show()
                                }
                            },
                            expanded = expandedFab
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center
            ) { innerPadding ->
                UrgentDialogs(urgentViewModel)

                UrgentScreen(
                    paddingValues = innerPadding,
                    isOverlapping = topBarScrollBehavior.state.overlappedFraction > 0f,
                    pagerState = pagerState,
                    listStates = listStates,
                    boardNamesMap = urgentViewModel.boardNamesMap,
                    tasks = urgentViewModel.tasks,
                    showCompletedTasks = urgentViewModel.showCompletedTasks,
                    onToggleShowCompletedTasks = {
                        urgentViewModel.onEvent(UrgentEvent.ToggleCompletedTasksVisibility)
                    },
                    onTaskClick = { onNavigateToEditTask(it.taskId) },
                    onPriorityClick = {
                        urgentViewModel.onEvent(UrgentEvent.ShowDialog(
                            type = UrgentEvent.DialogType.Priority(it)
                        ))
                    },
                    onDateTimeClick = {
                        urgentViewModel.onEvent(UrgentEvent.ShowTaskDateTimeFullDialog(it))
                    },
                    onToggleTaskCompleted = {
                        urgentViewModel.onEvent(UrgentEvent.ToggleTaskCompleted(it))
                    },
                    onSubtaskClick = {
                        onNavigateToEditSubtask(urgentViewModel.selectedBoard!!.boardId, it.subtaskId)
                    },
                    onToggleSubtaskCompleted = {
                        urgentViewModel.onEvent(UrgentEvent.ToggleSubtaskCompleted(it))
                    },
                    onBoardNameClick = {
                        urgentViewModel.onEvent(UrgentEvent.ChangeBottomSheetContent(
                            UrgentEvent.BottomSheetContent.BoardsList(it)
                        ))
                        scope.launch {
                            if (urgentViewModel.selectedBoard == null) return@launch
                            bottomSheetListState.scrollToItem(
                                index = urgentViewModel.boards.indexOf(urgentViewModel.selectedBoard)
                            )
                            bottomSheetState.show()
                        }
                    }
                )
            }
            AnimatedVisibility(
                visible = urgentViewModel.selectedTask != null,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it }
            ) {
                TaskDateTimeFullDialog(
                    task = urgentViewModel.selectedTask,
                    date = urgentViewModel.selectedDate,
                    onDateFieldClick = {
                        urgentViewModel.onEvent(UrgentEvent.ShowDialog(
                            type = UrgentEvent.DialogType.TaskDate
                        ))
                    },
                    onDateRemove = {
                        urgentViewModel.onEvent(UrgentEvent.ChangeDate(date = null))
                        urgentViewModel.onEvent(UrgentEvent.ChangeTime(time = null))
                    },
                    time = urgentViewModel.selectedTime,
                    onTimeFieldClick = {
                        urgentViewModel.onEvent(UrgentEvent.ShowDialog(
                            type = UrgentEvent.DialogType.TaskTime
                        ))
                    },
                    onTimeRemove = {
                        urgentViewModel.onEvent(UrgentEvent.ChangeTime(time = null))
                    },
                    onAlertChangesMade = {
                        urgentViewModel.onEvent(UrgentEvent.ShowDialog(
                            type = UrgentEvent.DialogType.DiscardChanges
                        ))
                    },
                    onCloseClick = {
                        urgentViewModel.onEvent(UrgentEvent.ShowTaskDateTimeFullDialog(task = null))
                    },
                    onSaveClick = {
                        urgentViewModel.onEvent(UrgentEvent.SaveTaskDateTimeChanges)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun UrgentScreen(
    paddingValues: PaddingValues = PaddingValues(),
    isOverlapping: Boolean = false,
    pagerState: PagerState = rememberPagerState(),
    listStates: List<LazyListState> = emptyList(),
    boardNamesMap: Map<Int, String> = mapOf(),
    tasks: Map<LocalDate?, List<TaskWithSubtasks>> = emptyMap(),
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
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column {
            UrgentTabs(state = pagerState, isTopBarCollapsed = isOverlapping)
            UrgentTabsContent(
                state = pagerState,
                listStates = listStates,
                boardNamesMap = boardNamesMap,
                tasks = tasks,
                showCompletedTasks = showCompletedTasks,
                onToggleShowCompletedTasks = onToggleShowCompletedTasks,
                onTaskClick = onTaskClick,
                onPriorityClick = onPriorityClick,
                onDateTimeClick = onDateTimeClick,
                onToggleTaskCompleted = onToggleTaskCompleted,
                onBoardNameClick = onBoardNameClick,
                onSubtaskClick = onSubtaskClick,
                onToggleSubtaskCompleted = onToggleSubtaskCompleted
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@CompletePreviews
@Composable
private fun UrgentScreenPreview() {
    FinitoTheme {
        Surface {
            UrgentScreen(
                tasks = TaskWithSubtasks.dummyTasks.groupBy { it.task.date }
            )
        }
    }
}