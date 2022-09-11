package com.example.finito.features.boards.presentation.board

import android.content.res.Configuration
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
import com.example.finito.core.presentation.HandleBackPress
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.features.boards.domain.BoardState
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
    boardViewModel: BoardViewModel = hiltViewModel(),
) {
    val detailedBoard = boardViewModel.board
    val boardState = when {
        detailedBoard?.board?.archived == false && !detailedBoard.board.deleted -> BoardState.Active
        detailedBoard?.board?.archived == true -> BoardState.Archived
        else -> BoardState.Deleted
    }

    val scope = rememberCoroutineScope()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()

    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    HandleBackPress(
        drawerState = if (boardState == BoardState.Active) drawerState else null,
        onBackPress = {
            if (boardState != BoardState.Active) {
                navController.navigateUp()
                return@HandleBackPress
            }
            navController.popBackStack(Screen.Home.route, inclusive = false)
        }
    )

    LaunchedEffect(Unit) {
        boardViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BoardViewModel.Event.ArchiveBoard -> {
                    navController.popBackStack(route = Screen.Home.route, inclusive = false)
                }
                BoardViewModel.Event.DeleteBoard -> {
                    navController.popBackStack(route = Screen.Home.route, inclusive = false)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BoardTopBar(
                onNavigationClick = onNavigationClick@{
                    if (boardState != BoardState.Active) {
                        navController.navigateUp()
                        return@onNavigationClick
                    }
                    scope.launch { drawerState.open() }
                },
                boardName = detailedBoard?.board?.name ?: "",
                boardState = boardState,
                scrollBehavior = topBarScrollBehavior,
                showMenu = boardViewModel.showScreenMenu,
                onMoreOptionsClick = {
                    boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = true))
                },
                onOptionClick = {
                    when (boardState) {
                        BoardState.Active -> when (it as ActiveBoardScreenOption) {
                            ActiveBoardScreenOption.EditBoard -> TODO(reason = "Navigate to edit board screen")
                            ActiveBoardScreenOption.ArchiveBoard -> {
                                boardViewModel.onEvent(BoardEvent.ArchiveBoard)
                            }
                            ActiveBoardScreenOption.DeleteBoard -> {
                                boardViewModel.onEvent((BoardEvent.DeleteBoard))
                            }
                            ActiveBoardScreenOption.DeleteCompletedTasks -> {
                                boardViewModel.onEvent(BoardEvent.DeleteCompletedTasks)
                            }
                        }
                        BoardState.Archived -> TODO()
                        BoardState.Deleted -> TODO()
                    }
                },
                onDismissMenu = {
                    boardViewModel.onEvent(BoardEvent.ShowScreenMenu(show = false))
                }
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
            item {
                RowToggle(
                    showContent = showCompletedTasks,
                    onShowContentToggle = onToggleShowCompletedTasks,
                    label = stringResource(id = R.string.show_completed_tasks, completedTasks.size),
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