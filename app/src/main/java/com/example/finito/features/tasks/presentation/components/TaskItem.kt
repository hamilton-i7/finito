package com.example.finito.features.tasks.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.Priority
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.util.isPast
import com.example.finito.features.tasks.domain.util.toFormattedChipDate
import com.example.finito.ui.theme.DisabledAlpha
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import com.google.accompanist.flowlayout.FlowRow
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
    ghostVariant: Boolean = false,
    boardName: String? = null,
    onTaskClick: () -> Unit = {},
    onCompletedToggle: () -> Unit = {},
    onPriorityClick: () -> Unit = {},
    onBoardNameClick: (() -> Unit)? = null,
    onDateTimeClick: () -> Unit = {},
    onDragging: () -> Unit = {},
    onDragEnd: () -> Unit = {},
) {
    val tonalElevation by animateDpAsState(targetValue = if (isDragging) 3.dp else 0.dp)

    LaunchedEffect(isDragging) {
        if (!isDragging) {
            onDragEnd()
            return@LaunchedEffect
        }
        onDragging()
    }

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
                    .clickable(onClick = onTaskClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (task.completed) {
                    IconButton(
                        onClick = onCompletedToggle,
                        modifier = if (isSimpleTask) Modifier else Modifier.offset(y = (-12).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(id = R.string.mark_as_uncompleted),
                            tint = finitoColors.primary,
                        )
                    }
                } else {
                    RadioButton(
                        selected = false,
                        onClick = onCompletedToggle,
                        enabled = !ghostVariant,
                        modifier = if (isSimpleTask) Modifier else Modifier.offset(y = (-12).dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.name,
                        color = if (ghostVariant) finitoColors.onSurface.copy(
                            alpha = DisabledAlpha
                        ) else finitoColors.onSurface,
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
                            color = if (ghostVariant) finitoColors.onSurfaceVariant.copy(
                                alpha = DisabledAlpha
                            ) else finitoColors.onSurfaceVariant,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 3,
                            textDecoration = if (task.completed)
                                TextDecoration.LineThrough
                            else null
                        )
                    }
                    if (!isSimpleTask) {
                        // TODO 22/09/2022: Fix spacing on the cross axis
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            mainAxisSpacing = 8.dp,
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
                                            color = finitoColors.primary
                                        )
                                    },
                                    enabled = !task.completed && !ghostVariant,
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
                                            enabled = !task.completed && !ghostVariant,
                                            border = null
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
                                            enabled = !task.completed && !ghostVariant,
                                            border = null
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
                                            enabled = !task.completed && !ghostVariant,
                                            border = null
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
                                    enabled = !task.completed && !ghostVariant,
                                    colors = InputChipDefaults.inputChipColors(
                                        labelColor = contentColor,
                                        trailingIconColor = contentColor,

                                        ),
                                    border = InputChipDefaults.inputChipBorder(
                                        borderColor = borderColor,
                                    )
                                )
                            }
                        }
                    }
                }
                if (showDragIndicator && !ghostVariant) {
                    Icon(
                        imageVector = Icons.Outlined.DragIndicator,
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
                ghostVariant = !item.completed,
                subtasksAmount = 4,
                isDragging = true
            )
        }
    }
}