package com.example.finito.features.tasks.presentation.screen.today

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
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.components.SortingChips
import com.example.finito.core.presentation.components.bars.SmallTopBarWithMenu
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.core.presentation.util.calculateDp
import com.example.finito.core.presentation.util.menu.TodayScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.presentation.components.BoardsListSheetContent
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.domain.util.toFullFormat
import com.example.finito.features.tasks.presentation.components.NewTaskSheetContent
import com.example.finito.features.tasks.presentation.components.TaskDateTimeFullDialog
import com.example.finito.features.tasks.presentation.components.TaskItem
import com.example.finito.features.tasks.presentation.screen.today.components.TodayDialogs
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    todayViewModel: TodayViewModel = hiltViewModel(),
    onNavigateToCreateTask: (boardId: Int, name: String) -> Unit = {_, _ -> },
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
        initialValue = ModalBottomSheetValue.Hidden
    )
    val bottomSheetCorners by animateDpAsState(
        targetValue = calculateDp(bottomSheetState)
    )
    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    BackHandler {
        if (bottomSheetState.isVisible) {
            scope.launch { bottomSheetState.hide() }
            return@BackHandler
        }
        finishActivity()
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = bottomSheetCorners, topEnd = bottomSheetCorners),
        sheetBackgroundColor = finitoColors.surface,
        sheetContent = sheetContent@{
            when (todayViewModel.bottomSheetContent) {
                is TodayEvent.BottomSheetContent.BoardsList -> {
                    BoardsListSheetContent(
                        state = bottomSheetListState,
                        boards = todayViewModel.boards,
                        selectedBoard = todayViewModel.selectedBoard,
                        onBoardClick = {
                            val task = (
                                    todayViewModel.bottomSheetContent as TodayEvent
                                    .BottomSheetContent
                                    .BoardsList
                            ).task
                            scope.launch { bottomSheetState.hide() }
                            todayViewModel.onEvent(TodayEvent.ChangeBoard(board = it, task = task))
                        }
                    )
                }
                TodayEvent.BottomSheetContent.NewTask -> {
                    NewTaskSheetContent(
                        nameTextFieldState = todayViewModel.newTaskNameState.copy(
                            onValueChange = {
                                todayViewModel.onEvent(TodayEvent.ChangeNewTaskName(it))
                            }),
                        focusRequester = focusRequester,
                        onViewMoreOptionsClick = {
                            onNavigateToCreateTask(
                                todayViewModel.selectedBoard!!.boardId,
                                todayViewModel.newTaskNameState.value
                            )
                            scope.launch { bottomSheetState.hide() }
                        },
                        onSaveClick = {
                            todayViewModel.onEvent(TodayEvent.SaveNewTask)
                            scope.launch { bottomSheetState.hide() }
                        },
                        saveButtonEnabled = todayViewModel.newTaskNameState.value.isNotBlank(),
                        includeBoardIndicator = true,
                        selectedBoardName = todayViewModel.selectedBoard?.name ?: "",
                        boardsMenuExpanded = bottomSheetState.isVisible,
                        onBoardIndicatorClick = {
                            todayViewModel.onEvent(TodayEvent.ChangeBottomSheetContent(
                                content = TodayEvent.BottomSheetContent.BoardsList()
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
                        title = stringResource(id = R.string.today),
                        showMenu = todayViewModel.showScreenMenu,
                        onDismissMenu = {
                            todayViewModel.onEvent(TodayEvent.ShowScreenMenu(show = false))
                        },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onMoreOptionsClick = {
                            todayViewModel.onEvent(TodayEvent.ShowScreenMenu(show = true))
                        },
                        options = listOf<TodayScreenMenuOption>(
                            TodayScreenMenuOption.DeleteCompleted
                        ),
                        onOptionClick = { option ->
                            todayViewModel.onEvent(TodayEvent.ShowScreenMenu(show = false))
                            when (option) {
                                TodayScreenMenuOption.DeleteCompleted -> {
                                    todayViewModel.onEvent(TodayEvent.ShowDialog(
                                        type = TodayEvent.DialogType.DeleteCompleted
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
                TodayDialogs(todayViewModel)

                TodayScreen(
                    paddingValues = innerPadding,
                    listState = listState,
                    selectedSortingOption = todayViewModel.sortingOption,
                    onSortingOptionClick = onSortingOptionClick@{
                        if (todayViewModel.sortingOption == it) {
                            todayViewModel.onEvent(TodayEvent.SortByPriority(option =  null))
                            return@onSortingOptionClick
                        }
                        todayViewModel.onEvent(TodayEvent.SortByPriority(it))
                    },
                    boardsMap = todayViewModel.boardsMap,
                    tasks = todayViewModel.tasks,
                    showCompletedTasks = todayViewModel.showCompletedTasks,
                    onToggleShowCompletedTasks = {
                        todayViewModel.onEvent(TodayEvent.ToggleCompletedTasksVisibility)
                    },
                    onTaskClick = { onNavigateToEditTask(it.task.taskId) },
                    onPriorityClick = {
                        todayViewModel.onEvent(TodayEvent.ShowDialog(
                            type = TodayEvent.DialogType.Priority(it)
                        ))
                    },
                    onDateTimeClick = {
                        todayViewModel.onEvent(TodayEvent.ShowTaskDateTimeFullDialog(it))
                    },
                    onToggleTaskCompleted = {
                        todayViewModel.onEvent(TodayEvent.ToggleTaskCompleted(it))
                    },
                    onBoardNameClick = {
                        todayViewModel.onEvent(TodayEvent.ChangeBottomSheetContent(
                            TodayEvent.BottomSheetContent.BoardsList(it)
                        ))
                        scope.launch {
                            if (todayViewModel.selectedBoard == null) return@launch
                            bottomSheetListState.scrollToItem(
                                index = todayViewModel.boards.indexOf(todayViewModel.selectedBoard)
                            )
                            bottomSheetState.show()
                        }
                    }
                )
            }
            AnimatedVisibility(
                visible = todayViewModel.selectedTask != null,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it }
            ) {
                TaskDateTimeFullDialog(
                    task = todayViewModel.selectedTask ?: TaskWithSubtasks.dummyTasks.random(),
                    date = todayViewModel.selectedDate,
                    onDateFieldClick = {
                        todayViewModel.onEvent(TodayEvent.ShowDialog(
                            type = TodayEvent.DialogType.TaskDate
                        ))
                    },
                    onDateRemove = {
                        todayViewModel.onEvent(TodayEvent.ChangeDate(date = null))
                        todayViewModel.onEvent(TodayEvent.ChangeTime(time = null))
                    },
                    time = todayViewModel.selectedTime,
                    onTimeFieldClick = {
                        todayViewModel.onEvent(TodayEvent.ShowDialog(
                            type = TodayEvent.DialogType.TaskTime
                        ))
                    },
                    onTimeRemove = {
                        todayViewModel.onEvent(TodayEvent.ChangeTime(time = null))
                    },
                    onAlertChangesMade = {
                        todayViewModel.onEvent(TodayEvent.ShowDialog(
                            type = TodayEvent.DialogType.DiscardChanges
                        ))
                    },
                    onCloseClick = {
                        todayViewModel.onEvent(TodayEvent.ShowTaskDateTimeFullDialog(task = null))
                    },
                    onSaveClick = {
                        todayViewModel.onEvent(TodayEvent.SaveTaskDateTimeChanges)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodayScreen(
    paddingValues: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
    selectedSortingOption: SortingOption.Priority? = null,
    onSortingOptionClick: (SortingOption.Priority) -> Unit = {},
    boardsMap: Map<Int, String> = mapOf(),
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 72.dp),
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
                    text = LocalDate.now().toFullFormat(locale, complete = true),
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
                    boardName = boardsMap[it.task.boardId],
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
                        boardName = boardsMap[it.task.boardId],
                        onTaskClick = { onTaskClick(it) },
                    )
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun TodayScreenPreview() {
    FinitoTheme {
        Surface {
            TodayScreen(tasks = TaskWithSubtasks.dummyTasks)
        }
    }
}