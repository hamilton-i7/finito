package com.example.finito.features.boards.presentation.board

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.presentation.components.CreateFab
import com.example.finito.features.boards.presentation.board.components.BoardDialogs
import com.example.finito.features.boards.presentation.board.components.BoardTopBar
import com.example.finito.features.tasks.domain.entity.CompletedTask
import com.example.finito.features.tasks.domain.entity.DetailedTask
import com.example.finito.features.tasks.presentation.components.CompletedTasksProgressBar
import com.example.finito.features.tasks.presentation.components.TaskItem
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    navController: NavController,
    drawerState: DrawerState,
    boardViewModel: BoardViewModel = hiltViewModel(),
) {
    val detailedBoard = boardViewModel.board

    val scope = rememberCoroutineScope()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()

    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    Scaffold(
        topBar = {
            BoardTopBar(
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                boardName = detailedBoard?.board?.name ?: "",
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
            }
        )
    }
}

@Composable
private fun BoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
    tasks: List<DetailedTask> = emptyList(),
    showCompletedTasks: Boolean = true,
    onToggleShowCompletedTasks: () -> Unit = {},
    onPriorityClick: (DetailedTask) -> Unit = {},
) {
    val completedTasks = tasks.filter { it.task.completed }
    val uncompletedTasks = tasks.filter { !it.task.completed }
    val interactionSource = remember { MutableInteractionSource() }

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
            items(uncompletedTasks, contentType = { "uncompleted tasks" }) {
                TaskItem(
                    detailedTask = it,
                    onPriorityClick = { onPriorityClick(it) }
                )
            }
            item {
                val rotate: Float by animateFloatAsState(if (showCompletedTasks) 0f else -180f)

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(),
                            onClick = onToggleShowCompletedTasks
                        )
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.completed, completedTasks.size)
                    )
                    Icon(
                        imageVector = Icons.Outlined.ExpandLess,
                        contentDescription = stringResource(
                            id = if (showCompletedTasks)
                                R.string.hide_completed_tasks
                            else
                                R.string.show_completed_tasks
                        ),
                        modifier = Modifier.rotate(rotate)
                    )
                }
            }
            items(completedTasks, contentType = { "completed tasks" }) {
                AnimatedVisibility(
                    visible = showCompletedTasks,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) { TaskItem(detailedTask = it) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenPreview() {
    FinitoTheme {
        Surface {
            BoardScreen(tasks = DetailedTask.dummyTasks)
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
            BoardScreen(tasks = DetailedTask.dummyTasks)
        }
    }
}