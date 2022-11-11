package com.example.finito.features.tasks.presentation.screen.urgent.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.TabOption
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import java.time.LocalDate

private val tabOptions = listOf(
    TabOption(icon = R.drawable.time_limit, contentDescription = R.string.with_date),
    TabOption(icon = R.drawable.calendar_free, contentDescription = R.string.without_date),
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun UrgentTabs(
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
) {
    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = state.currentPage,
        modifier = modifier
    ) {
        tabOptions.forEachIndexed { index, tabOption ->
            Tab(
                selected = state.currentPage == index,
                onClick = { scope.launch { state.animateScrollToPage(index) } },
                icon = {
                    Icon(
                        painter = painterResource(id = tabOption.icon),
                        contentDescription = stringResource(id = tabOption.contentDescription)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun UrgentTabsContent(
    state: PagerState = rememberPagerState(),
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
    HorizontalPager(
        count = tabOptions.size,
        state = state,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            0 -> UrgentTasksWithDate(
                listState = listStates.getOrElse(0) { rememberLazyListState() },
                boardNamesMap = boardNamesMap,
                tasks = tasks.filterKeys { it != null },
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
            1 -> UrgentTasksWithoutDate(
                listState = listStates.getOrElse(1) { rememberLazyListState() },
                boardNamesMap = boardNamesMap,
                tasks = tasks.getOrDefault(key = null, emptyList()),
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