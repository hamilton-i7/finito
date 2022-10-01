package com.example.finito.features.tasks.presentation.screen.urgent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.presentation.components.TaskItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UrgentTasksWithoutDate(
    listState: LazyListState = rememberLazyListState(),
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
    val completedTasks = tasks.filterCompleted()
    val uncompletedTasks = tasks.filterUncompleted()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
    ) {
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