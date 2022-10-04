package com.example.finito.features.tasks.presentation.screen.urgent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.components.RowToggle
import com.example.finito.core.presentation.util.AnimationDurationConstants.LongDurationMillis
import com.example.finito.core.presentation.util.AnimationDurationConstants.RegularDurationMillis
import com.example.finito.core.presentation.util.AnimationDurationConstants.ShortestDurationMillis
import com.example.finito.core.presentation.util.LazyListKeys
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.filterCompleted
import com.example.finito.features.subtasks.domain.entity.filterUncompleted
import com.example.finito.features.subtasks.presentation.components.SubtaskItem
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.entity.filterCompleted
import com.example.finito.features.tasks.domain.entity.filterUncompleted
import com.example.finito.features.tasks.domain.util.toFullFormat
import com.example.finito.features.tasks.presentation.components.TaskItem
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UrgentTasksWithDate(
    listState: LazyListState = rememberLazyListState(),
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
    val locale = LocalConfiguration.current.locales[0]
    val uncompletedTasks = tasks.mapValues { it.value.filterUncompleted() }

    val tasksWithNoCompletedSubtasks = uncompletedTasks.mapValues {
        it.value.map { taskWithSubtasks ->
            taskWithSubtasks.copy(subtasks = taskWithSubtasks.subtasks.filterUncompleted())
        }
    }
    val tasksWithCompletedSubtasks = uncompletedTasks.mapValues {
        it.value.filter { taskWithSubtasks ->
            taskWithSubtasks.subtasks.filterCompleted().isNotEmpty()
        }.map { taskWithSubtasks ->
            taskWithSubtasks.copy(subtasks = taskWithSubtasks.subtasks.filterCompleted())
        }
    }.values.flatten()

    val completedTasks = tasks.values.flatten().filterCompleted()
    val completedTasksAmount = tasksWithCompletedSubtasks.flatMap { it.subtasks }.size
        .plus(completedTasks.flatMap { it.subtasks }.size)
        .plus(completedTasks.size)

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
    ) {
        tasksWithNoCompletedSubtasks.forEach { (date, tasks) ->
            if (tasks.any { !it.task.completed }) {
                item {
                    Text(
                        text = date!!.toFullFormat(locale, complete = true),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    )
                }
            }
            tasks.forEach { (task, subtasks) ->
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
                            durationMillis = LongDurationMillis,
                            delayMillis = ShortestDurationMillis
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = RegularDurationMillis)
                    ),
                    modifier = Modifier.animateItemPlacement()
                ) {
                    TaskItem(
                        task = task,
                        boardName = boardNamesMap[task.boardId],
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
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = LongDurationMillis,
                            delayMillis = ShortestDurationMillis
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = RegularDurationMillis)
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
                            durationMillis = LongDurationMillis,
                            delayMillis = ShortestDurationMillis
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = RegularDurationMillis)
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
                            durationMillis = LongDurationMillis,
                            delayMillis = ShortestDurationMillis
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = RegularDurationMillis)
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