package com.example.finito.features.tasks.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.features.tasks.domain.util.isPast
import com.example.finito.features.tasks.domain.util.toFormattedChipDate
import com.example.finito.ui.theme.DisabledAlpha
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    subtasksAmount: Int = 0,
    isDragging: Boolean = false,
    showDragIndicator: Boolean = false,
    enabled: Boolean = true,
    boardName: String? = null,
    onTaskClick: () -> Unit = {},
    onCompletedToggle: () -> Unit = {},
    onPriorityClick: () -> Unit = {},
    onBoardNameClick: (() -> Unit)? = null,
    onDateTimeClick: () -> Unit = {},
) {
    val tonalElevation by animateDpAsState(targetValue = if (isDragging) 3.dp else 0.dp)

    Surface(
        tonalElevation = tonalElevation,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        val locale = LocalConfiguration.current.locales[0]
        val context = LocalContext.current
        val isSimpleTask = task.description == null
                && task.date == null
                && task.priority == null

        Box {
            Row(
                verticalAlignment = if (isSimpleTask) Alignment.CenterVertically else Alignment.Top,
                modifier = Modifier
                    .clickable(enabled = enabled, onClick = onTaskClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (task.completed) {
                    IconButton(
                        onClick = onCompletedToggle,
                        enabled = enabled,
                        modifier = if (isSimpleTask) Modifier else Modifier.offset(y = (-12).dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.done),
                            contentDescription = stringResource(id = R.string.mark_as_uncompleted),
                            tint = if (enabled) finitoColors.primary
                            else finitoColors.primary.copy(alpha = DisabledAlpha),
                        )
                    }
                } else {
                    RadioButton(
                        selected = false,
                        onClick = onCompletedToggle,
                        enabled = enabled,
                        modifier = if (isSimpleTask) Modifier else Modifier.offset(y = (-12).dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.name,
                        color = if (enabled) finitoColors.onSurface
                        else finitoColors.onSurface.copy(alpha = DisabledAlpha),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        textDecoration = if (task.completed)
                            TextDecoration.LineThrough
                        else null
                    )
                    if (task.description != null) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (enabled) finitoColors.onSurfaceVariant
                            else finitoColors.onSurfaceVariant.copy(alpha = DisabledAlpha),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 3,
                            textDecoration = if (task.completed)
                                TextDecoration.LineThrough
                            else null
                        )
                    }
                    if (!isSimpleTask) {
                        val chipHeight = 32.dp

                        FlowRow(
                            mainAxisSize = SizeMode.Expand,
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            if (boardName != null) {
                                InputChip(
                                    selected = true,
                                    onClick = onClick@{
                                        if (onBoardNameClick == null) return@onClick
                                        onBoardNameClick()
                                    },
                                    label = {
                                        Text(
                                            text = boardName,
                                            color = if (enabled)
                                                finitoColors.primary
                                            else
                                                finitoColors.outline.copy(alpha = DisabledAlpha)
                                        )
                                    },
                                    enabled = !task.completed && enabled,
                                    modifier = Modifier.height(chipHeight)
                                )
                            }
                            if (task.priority != null) {
                                when (task.priority) {
                                    Priority.LOW -> {
                                        InputChip(
                                            onClick = onPriorityClick,
                                            selected = false,
                                            label = {
                                                Text(text = stringResource(id = task.priority.label))
                                            },
                                            colors = InputChipDefaults.inputChipColors(
                                                labelColor = finitoColors.onLowPriorityContainer,
                                                containerColor = finitoColors.lowPriorityContainer,
                                                disabledContainerColor = finitoColors.lowPriorityContainer.copy(alpha = 0.25f),
                                            ),
                                            enabled = !task.completed && enabled,
                                            border = null,
                                            modifier = Modifier.height(chipHeight)
                                        )
                                    }
                                    Priority.MEDIUM -> {
                                        InputChip(
                                            onClick = onPriorityClick,
                                            selected = false,
                                            label = {
                                                Text(text = stringResource(id = task.priority.label))
                                            },
                                            colors = InputChipDefaults.inputChipColors(
                                                labelColor = finitoColors.onMediumPriorityContainer,
                                                containerColor = finitoColors.mediumPriorityContainer,
                                                disabledContainerColor = finitoColors.mediumPriorityContainer.copy(alpha = 0.25f),
                                            ),
                                            enabled = !task.completed && enabled,
                                            border = null,
                                            modifier = Modifier.height(chipHeight)
                                        )
                                    }
                                    Priority.URGENT -> {
                                        InputChip(
                                            onClick = onPriorityClick,
                                            selected = false,
                                            label = {
                                                Text(text = stringResource(id = task.priority.label))
                                            },
                                            colors = InputChipDefaults.inputChipColors(
                                                labelColor = finitoColors.onUrgentPriorityContainer,
                                                containerColor = finitoColors.urgentPriorityContainer,
                                                disabledContainerColor = finitoColors.urgentPriorityContainer.copy(alpha = 0.25f),
                                            ),
                                            enabled = !task.completed && enabled,
                                            border = null,
                                            modifier = Modifier.height(chipHeight)
                                        )
                                    }
                                }
                            }
                            if (task.date != null) {
                                val isPastDate = task.date.isBefore(LocalDate.now())
                                val isPastTime = task.time?.let {
                                    LocalDateTime.of(task.date, it).isPast()
                                } ?: false
                                val contentColor: Color
                                val borderColor: Color
                                if (isPastDate || isPastTime) {
                                    contentColor = finitoColors.error
                                    borderColor = finitoColors.error
                                } else {
                                    contentColor = finitoColors.onSurfaceVariant
                                    borderColor = finitoColors.outline
                                }

                                InputChip(
                                    selected = false,
                                    onClick = onDateTimeClick,
                                    label = {
                                        Text(text = toFormattedChipDate(
                                            date = task.date,
                                            time = task.time,
                                            context = context,
                                            locale = locale
                                        ))
                                    },
                                    enabled = !task.completed && enabled,
                                    colors = InputChipDefaults.inputChipColors(
                                        labelColor = contentColor,
                                        trailingIconColor = contentColor,

                                        ),
                                    border = InputChipDefaults.inputChipBorder(
                                        borderColor = borderColor,
                                    ),
                                    modifier = Modifier.height(chipHeight)
                                )
                            }
                        }
                    }
                }
                if (showDragIndicator && enabled) {
                    Icon(
                        painter = painterResource(id = R.drawable.drag_reorder),
                        contentDescription = stringResource(id = R.string.reorder_task)
                    )
                }
            }
            AnimatedVisibility(
                visible = isDragging && subtasksAmount != 0,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(
                    color = finitoColors.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.plural_subtask_indicator,
                            subtasksAmount,
                            subtasksAmount
                        ),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun TaskItemPreview() {
    val item = Task.dummyTasks.random()
    FinitoTheme {
        Surface {
            TaskItem(
                task = item,
                enabled = item.completed,
                subtasksAmount = 4,
                isDragging = true
            )
        }
    }
}