package com.example.finito.features.boards.presentation.board

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.domain.util.menu.ActiveBoardScreenOption
import com.example.finito.core.domain.util.menu.ArchivedBoardScreenMenuOption
import com.example.finito.core.domain.util.menu.DeletedBoardScreenMenuOption
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.features.boards.presentation.SharedBoardEvent
import com.example.finito.features.boards.presentation.SharedBoardViewModel
import com.example.finito.features.boards.presentation.board.components.BoardDialogs
import com.example.finito.features.boards.presentation.board.components.BoardTopBar
import com.example.finito.features.tasks.domain.entity.CompletedTask
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.presentation.components.CompletedTasksProgressBar
import com.example.finito.features.tasks.presentation.components.TaskItem
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    navController: NavController,
    drawerState: DrawerState,
    sharedBoardViewModel: SharedBoardViewModel,
    boardViewModel: BoardViewModel = hiltViewModel(),
    showSnackbar: (message: Int, onActionClick: () -> Unit) -> Unit,
) {
    val detailedBoard = boardViewModel.board
    val activeBoard = detailedBoard?.board?.archived == false && !detailedBoard.board.deleted
    val previousRoute = navController.previousBackStackEntry?.destination?.route

    val scope = rememberCoroutineScope()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()

    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        if (previousRoute == Screen.Archive.route
            || previousRoute == Screen.Trash.route) {
            navController.navigateUp()
            return@BackHandler
        }
        navController.navigate(route = Screen.Home.route) {
            popUpTo(route = Screen.Home.route) { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        boardViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BoardViewModel.Event.ShowSnackbar -> {
                    showSnackbar(event.message) {
                        sharedBoardViewModel.onEvent(SharedBoardEvent.ResetBoard(
                            board = event.board
                        ))
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BoardTopBar(
                onNavigationClick = onNavigationClick@{
                    if (!activeBoard) {
                        navController.navigateUp()
                        return@onNavigationClick
                    }
                    scope.launch { drawerState.open() }
                },
                boardName = detailedBoard?.board?.name ?: "",
                showMenu = boardViewModel.showScreenMenu,
                onMoreOptionsClick = {
                    boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = true))
                },
                onDismissMenu = {
                    boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = false))
                },
                onOptionClick = {
                    boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = false))

                    when (previousRoute) {
                        Screen.Archive.route -> {
                            when (it as ArchivedBoardScreenMenuOption) {
                                ArchivedBoardScreenMenuOption.DeleteBoard -> {
                                    boardViewModel.onEvent(BoardEvent.DeleteBoard)
                                    navController.navigateUp()
                                }
                                ArchivedBoardScreenMenuOption.DeleteCompletedTasks -> {
                                    boardViewModel.onEvent(BoardEvent.DeleteCompletedTasks)
                                }
                                ArchivedBoardScreenMenuOption.EditBoard -> {
                                    TODO(reason = "Navigate to edit screen")
                                }
                                ArchivedBoardScreenMenuOption.UnarchiveBoard -> {
                                    boardViewModel.onEvent(BoardEvent.RestoreBoard)
                                    navController.navigateUp()
                                }
                            }
                        }
                        Screen.Trash.route -> {
                            when (it as DeletedBoardScreenMenuOption) {
                                DeletedBoardScreenMenuOption.DeleteCompletedTasks -> {
                                    boardViewModel.onEvent(BoardEvent.DeleteCompletedTasks)
                                }
                                DeletedBoardScreenMenuOption.EditBoard -> {
                                    TODO(reason = "Navigate to edit screen")
                                }
                                DeletedBoardScreenMenuOption.RestoreBoard -> {
                                    boardViewModel.onEvent(BoardEvent.RestoreBoard)
                                    navController.navigateUp()
                                }
                            }
                        }
                        else -> {
                            when (it as ActiveBoardScreenOption) {
                                ActiveBoardScreenOption.ArchiveBoard -> {
                                    boardViewModel.onEvent(BoardEvent.ArchiveBoard)
                                    navController.navigate(route = Screen.Home.route) {
                                        popUpTo(route = Screen.Home.route) { inclusive = true }
                                    }
                                }
                                ActiveBoardScreenOption.DeleteBoard -> {
                                    boardViewModel.onEvent(BoardEvent.DeleteBoard)
                                    navController.navigate(route = Screen.Home.route) {
                                        popUpTo(route = Screen.Home.route) { inclusive = true }
                                    }
                                }
                                ActiveBoardScreenOption.DeleteCompletedTasks -> {
                                    boardViewModel.onEvent(BoardEvent.DeleteCompletedTasks)
                                }
                                ActiveBoardScreenOption.EditBoard -> {
                                    TODO(reason = "Navigate to edit screen")
                                }
                            }
                        }
                    }
                },
                previousRoute = previousRoute,
                scrollBehavior = topBarScrollBehavior
            )
        },
        floatingActionButton = {
            CreateFab(
                text = R.string.create_task,
                onClick = { /*TODO*/ },
                expanded = expandedFab
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (boardViewModel.showDialog) {
            BoardDialogs(boardViewModel)
        }

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
            onDateTimeClick = { navController.navigate(it) }
        )
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
    onDateTimeClick: (route: String) -> Unit = {},
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
                ) {
                    val route = "${Screen.TaskDateTime.prefix}/${it.task.taskId}"
                    onDateTimeClick(route)
                }
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

@Preview(showBackground = true)
@Composable
private fun BoardScreenPreview() {
    FinitoTheme {
        Surface {
            BoardScreen(tasks = TaskWithSubtasks.dummyTasks)
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BoardScreenPreviewDark() {
    FinitoTheme {
        Surface {
            BoardScreen(tasks = TaskWithSubtasks.dummyTasks)
        }
    }
}