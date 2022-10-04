package com.example.finito.features.subtasks.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.ui.theme.DisabledAlpha
import com.example.finito.ui.theme.finitoColors

@Composable
fun SubtaskItem(
    subtask: Subtask,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    enabled: Boolean = true,
    showDragIndicator: Boolean = false,
    onSubtaskClick: () -> Unit = {},
    onCompletedToggle: () -> Unit = {},
) {
    val isSimpleSubtask = subtask.description == null
    val tonalElevation by animateDpAsState(targetValue = if (isDragging) 3.dp else 0.dp)

    Surface(
        tonalElevation = tonalElevation,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Row(
            verticalAlignment = if (isSimpleSubtask) Alignment.CenterVertically else Alignment.Top,
            modifier = Modifier
                .clickable(enabled = enabled, onClick = onSubtaskClick)
                .padding(start = 48.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
        ) {
            if (subtask.completed) {
                IconButton(
                    onClick = onCompletedToggle,
                    enabled = enabled,
                    modifier = if (isSimpleSubtask) Modifier else Modifier.offset(y = (-12).dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
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
                    modifier = if (isSimpleSubtask) Modifier else Modifier.offset(y = (-12).dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subtask.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    textDecoration = if (subtask.completed)
                        TextDecoration.LineThrough
                    else null,
                    color = if (enabled) finitoColors.onSurface
                    else finitoColors.onSurface.copy(alpha = DisabledAlpha),
                )
                if (!isSimpleSubtask) {
                    Text(
                        text = subtask.description!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) finitoColors.onSurfaceVariant
                        else finitoColors.onSurfaceVariant.copy(alpha = DisabledAlpha),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                        textDecoration = if (subtask.completed)
                            TextDecoration.LineThrough
                        else null,
                    )
                }
            }
            if (showDragIndicator && enabled) {
                Icon(
                    imageVector = Icons.Outlined.DragIndicator,
                    contentDescription = stringResource(id = R.string.reorder_task)
                )
            }
        }
    }
}