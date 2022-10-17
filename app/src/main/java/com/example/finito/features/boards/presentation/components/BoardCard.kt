package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.components.PercentageIndicator
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.core.presentation.util.menu.BoardCardMenuOption
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardCard(
    onClick: () -> Unit,
    board: BoardWithLabelsAndTasks,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    onOptionsClick: () -> Unit = {},
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    options: List<BoardCardMenuOption> = emptyList(),
    onMenuItemClick: (BoardCardMenuOption) -> Unit = {},
) {
    val tasksAmount = board.tasks.size + board.tasks.flatMap { it.subtasks }.size
    val completedTasksProgress = with(board.tasks) {
        if (isEmpty()) return@with 0F
        val completedTasks = filter { it.completed }.size
        val completedSubtasks = flatMap { it.subtasks }.filter { it.completed }.size
        val completedAmount = completedTasks + completedSubtasks
        completedAmount / tasksAmount.toFloat()
    }
    val labelNames = board.labels.sortedBy { it.normalizedName }.joinToString { it.name }

    Card(
        onClick = onClick,
        border = if (isDragging) BorderStroke(
            width = 2.dp,
            color = finitoColors.tertiary
        ) else null,
        modifier = Modifier
            .testTag(TestTags.BOARD_CARD)
            .then(modifier)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PercentageIndicator(
                    progress = completedTasksProgress,
                    modifier = Modifier.padding(start = 16.dp)
                )
                IconButton(
                    onClick = onOptionsClick,
                    modifier = Modifier.testTag(TestTags.CARD_MENU_BUTTON)
                ) {
                    Box {
                        Icon(
                            painter = painterResource(id = R.drawable.menu_vertical),
                            contentDescription = stringResource(id = R.string.more_options),
                            tint = finitoColors.onSurfaceVariant
                        )
                        FinitoMenu(
                            show = showMenu,
                            onDismiss = onDismissMenu,
                            options = options,
                            onOptionClick = {
                                onMenuItemClick(it)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(id = R.string.tasks_amount, tasksAmount),
                    color = finitoColors.outline
                )
                Text(
                    text = board.board.name,
                    color = finitoColors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = labelNames.ifEmpty { " " },
                    color = finitoColors.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BoardCardPreview() {
    FinitoTheme {
        BoardCard(
            onClick = {},
            board = BoardWithLabelsAndTasks.dummyBoards.random()
        )
    }
}

@ThemePreviews
@Composable
private fun BoardCardDraggingPreview() {
    FinitoTheme {
        BoardCard(
            onClick = {},
            board = BoardWithLabelsAndTasks.dummyBoards.random(),
            isDragging = true
        )
    }
}