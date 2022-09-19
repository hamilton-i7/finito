package com.example.finito.features.boards.presentation.screen.board

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.components.textfields.BasicTextField
import com.example.finito.core.presentation.util.menu.ActiveBoardScreenOption
import com.example.finito.core.presentation.util.menu.ArchivedBoardScreenMenuOption
import com.example.finito.core.presentation.util.menu.DeletedBoardScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.screen.board.components.BoardDialogs
import com.example.finito.features.boards.presentation.screen.board.components.BoardTopBar
import com.example.finito.features.tasks.domain.entity.CompletedTask
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.presentation.components.CompletedTasksProgressBar
import com.example.finito.features.tasks.presentation.components.TaskDateTimeFullDialog
import com.example.finito.features.tasks.presentation.components.TaskItem
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToEditBoard: (boardId: Int, boardState: BoardState) -> Unit = {_, _ -> },
) {
    val detailedBoard = boardViewModel.board

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
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
                is BoardViewModel.Event.ShowSnackbar -> {
                    showSnackbar(event.message, R.string.undo) {
                        appViewModel.onEvent(AppEvent.UndoBoardChange(
                            board = event.board
                        ))
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
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                BasicTextField(
                    textFieldState = boardViewModel.newTaskNameState.copy(
                        onValueChange = {
                            boardViewModel.onEvent(BoardEvent.ChangeNewTaskName(it))
                        }),
                    placeholder = R.string.new_task,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onNavigateToCreateTask) {
                        Text(text = stringResource(id = R.string.view_more_options))
                    }

                    TextButton(
                        onClick = {
                            boardViewModel.onEvent(BoardEvent.SaveTask)
                            scope.launch { bottomSheetState.hide() }
                        },
                        enabled = boardViewModel.newTaskNameState.value.isNotBlank()
                    ) { Text(text = stringResource(id = R.string.save)) }
                }
            }
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
                                            boardViewModel.onEvent(BoardEvent.DeleteCompletedTasks)
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
                                            boardViewModel.onEvent(BoardEvent.DeleteCompletedTasks)
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
                                            boardViewModel.onEvent(BoardEvent.DeleteCompletedTasks)
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
                    listState = listState,
                    tasks = boardViewModel.board?.tasks ?: emptyList(),
                    showCompletedTasks = boardViewModel.showCompletedTasks,
                    onToggleShowCompletedTasks = {
                        boardViewModel.onEvent(BoardEvent.ToggleCompletedTasksVisibility)
                    },
                    onPriorityClick = {
                        boardViewModel.onEvent(BoardEvent.ShowDialog(
                            type = BoardEvent.DialogType.Priority(it)
                        ))
                    },
                    onDateTimeClick = {
                        boardViewModel.onEvent(BoardEvent.ShowTaskDateTimeFullDialog(it))
                    }
                )
            }

            AnimatedVisibility(
                visible = boardViewModel.selectedTask != null,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it }
            ) {
                TaskDateTimeFullDialog(
                    task = boardViewModel.selectedTask ?: TaskWithSubtasks.dummyTasks.random(),
                    date = boardViewModel.selectedDate,
                    onDateFieldClick = {
                        boardViewModel.onEvent(BoardEvent.ShowDialog(
                            type = BoardEvent.DialogType.TaskDate
                        ))
                    },
                    onDateRemove = { /*TODO*/ },
                    time = boardViewModel.selectedTime,
                    onTimeFieldClick = {
                        boardViewModel.onEvent(BoardEvent.ShowDialog(
                            type = BoardEvent.DialogType.TaskTime
                        ))
                    },
                    onTimeRemove = { /*TODO*/ },
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

@Composable
private fun BoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
    tasks: List<TaskWithSubtasks> = emptyList(),
    showCompletedTasks: Boolean = true,
    onToggleShowCompletedTasks: () -> Unit = {},
    onPriorityClick: (TaskWithSubtasks) -> Unit = {},
    onDateTimeClick: (TaskWithSubtasks) -> Unit = {},
) {
    val completedTasks = tasks.filter { it.task.completed }
    val uncompletedTasks = tasks.filter { !it.task.completed }

    // TODO: Show board labels
    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        LazyColumn(
            contentPadding = PaddingValues(top = 12.dp, bottom = 72.dp),
            state = listState,
        ) {
            item(contentType = "progress bar") {
                CompletedTasksProgressBar(
                    tasks = tasks.map { CompletedTask(completed = it.task.completed) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }
            items(
                items = uncompletedTasks,
                contentType = { "uncompleted tasks" },
                key = { it.task.taskId }
            ) {
                TaskItem(
                    task = it.task,
                    onPriorityClick = { onPriorityClick(it) }
                ) { onDateTimeClick(it) }
            }
            if (completedTasks.isNotEmpty()) {
                item {
                    RowToggle(
                        showContent = showCompletedTasks,
                        onShowContentToggle = onToggleShowCompletedTasks,
                        label = stringResource(id = R.string.completed, completedTasks.size),
                        showContentDescription = R.string.show_completed_tasks,
                        hideContentDescription = R.string.hide_completed_tasks
                    )
                }
                items(
                    items = completedTasks,
                    contentType = { "completed tasks" },
                    key = { it.task.taskId }
                ) {
                    AnimatedVisibility(
                        visible = showCompletedTasks,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) { TaskItem(task = it.task) }
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
            BoardScreen(tasks = TaskWithSubtasks.dummyTasks)
        }
    }
}