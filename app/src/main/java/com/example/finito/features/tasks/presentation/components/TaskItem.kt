package com.example.finito.features.tasks.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.Priority
import com.example.finito.features.tasks.domain.entity.DetailedTask
import com.example.finito.features.tasks.domain.util.toFormattedChipDate
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    detailedTask: DetailedTask,
    onTaskClick: () -> Unit = {},
    onCompletedToggle: () -> Unit = {},
    onPriorityClick: () -> Unit = {},
    onBoardNameClick: (() -> Unit)? = null,
    onDateTimeClick: () -> Unit = {},
) {
    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current

    Surface(
        onClick = onTaskClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (detailedTask.task.completed) {
                IconButton(
                    onClick = onCompletedToggle,
                    modifier = Modifier.offset(y = (-12).dp)
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
                    modifier = Modifier.offset(y = (-12).dp)
                )
            }
            Column {
                Text(
                    text = detailedTask.task.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    textDecoration = if (detailedTask.task.completed)
                        TextDecoration.LineThrough
                    else null
                )
                if (detailedTask.task.description != null) {
                    Text(
                        text = detailedTask.task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = finitoColors.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                        textDecoration = if (detailedTask.task.completed)
                            TextDecoration.LineThrough
                        else null
                    )
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    if (onBoardNameClick != null) {
                        InputChip(
                            selected = true,
                            onClick = onBoardNameClick,
                            label = {
                                Text(
                                    text = detailedTask.board.name,
                                    color = finitoColors.primary
                                )
                            },
                            enabled = !detailedTask.task.completed,
                        )
                    }
                    if (detailedTask.task.priority != null) {
                        when (detailedTask.task.priority) {
                            Priority.LOW -> {
                                InputChip(
                                    onClick = onPriorityClick,
                                    selected = false,
                                    label = {
                                        Text(text = stringResource(id = detailedTask.task.priority.label))
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        labelColor = finitoColors.onLowPriorityContainer,
                                        containerColor = finitoColors.lowPriorityContainer,
                                        disabledContainerColor = finitoColors.lowPriorityContainer.copy(alpha = 0.25f),
                                    ),
                                    enabled = !detailedTask.task.completed,
                                    border = null
                                )
                            }
                            Priority.MEDIUM -> {
                                InputChip(
                                    onClick = onPriorityClick,
                                    selected = false,
                                    label = {
                                        Text(text = stringResource(id = detailedTask.task.priority.label))
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        labelColor = finitoColors.onMediumPriorityContainer,
                                        containerColor = finitoColors.mediumPriorityContainer,
                                        disabledContainerColor = finitoColors.mediumPriorityContainer.copy(alpha = 0.25f),
                                    ),
                                    enabled = !detailedTask.task.completed,
                                    border = null
                                )
                            }
                            Priority.URGENT -> {
                                InputChip(
                                    onClick = onPriorityClick,
                                    selected = false,
                                    label = {
                                        Text(text = stringResource(id = detailedTask.task.priority.label))
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        labelColor = finitoColors.onUrgentPriorityContainer,
                                        containerColor = finitoColors.urgentPriorityContainer,
                                        disabledContainerColor = finitoColors.urgentPriorityContainer.copy(alpha = 0.25f),
                                    ),
                                    enabled = !detailedTask.task.completed,
                                    border = null
                                )
                            }
                        }
                    }
                    if (detailedTask.task.date != null) {
                        InputChip(
                            selected = false,
                            onClick = onDateTimeClick,
                            label = {
                                Text(text = toFormattedChipDate(
                                    date = detailedTask.task.date,
                                    time = detailedTask.task.time,
                                    context = context,
                                    locale = locale
                                ))
                            },
                            enabled = !detailedTask.task.completed,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemPreview() {

    FinitoTheme {
        Surface {
            TaskItem(detailedTask = DetailedTask.dummyTasks.random())
        }
    }
}