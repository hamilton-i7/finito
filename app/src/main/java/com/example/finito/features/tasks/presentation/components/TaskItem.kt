package com.example.finito.features.tasks.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.Priority
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.util.toFormattedChipDate
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import com.google.accompanist.flowlayout.FlowRow
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    isDragging: Boolean = false,
    boardName: String? = null,
    onTaskClick: () -> Unit = {},
    onCompletedToggle: () -> Unit = {},
    onPriorityClick: () -> Unit = {},
    onBoardNameClick: (() -> Unit)? = null,
    onDateTimeClick: () -> Unit = {},
) {
    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current
    val isSimpleTask = task.description == null
            && task.date == null
            && task.priority == null
    val tonalElevation by animateDpAsState(targetValue = if (isDragging) 3.dp else 0.dp)

    LaunchedEffect(isDragging) {
        if (!isDragging) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // TODO: Add Drag handle icon
    Surface(
        onClick = onTaskClick,
        tonalElevation = tonalElevation,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Row(
            verticalAlignment = if (isSimpleTask) Alignment.CenterVertically else Alignment.Top,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                    modifier = if (isSimpleTask) Modifier else Modifier.offset(y = (-12).dp)
                )
            }
            Column(modifier = Modifier.fillMaxHeight()) {
                Text(
                    text = task.name,
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
                        color = finitoColors.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                        textDecoration = if (task.completed)
                            TextDecoration.LineThrough
                        else null
                    )
                }
                if (!isSimpleTask) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
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
                                enabled = !task.completed,
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
                                        enabled = !task.completed,
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
                                        enabled = !task.completed,
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
                                        enabled = !task.completed,
                                        border = null
                                    )
                                }
                            }
                        }
                        if (task.date != null) {
                            val isPast = task.date.isBefore(LocalDate.now())
                            val contentColor: Color
                            val borderColor: Color
                            if (isPast) {
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
                                enabled = !task.completed,
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
        }
    }
}

@ThemePreviews
@Composable
private fun TaskItemPreview() {

    FinitoTheme {
        Surface {
            TaskItem(task = Task.dummyTasks.random())
        }
    }
}