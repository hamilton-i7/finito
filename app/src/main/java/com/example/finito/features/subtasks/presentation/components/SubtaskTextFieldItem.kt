package com.example.finito.features.subtasks.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.components.textfields.BasicTextField
import com.example.finito.core.presentation.util.TextFieldState
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress

@Composable
fun SubtaskTextFieldItem(
    state: TextFieldState,
    reorderableState: ReorderableLazyListState,
    modifier: Modifier = Modifier,
    onRemoveSubtask: () -> Unit = {},
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    isDragging: Boolean = false,
) {
    LaunchedEffect(isDragging) {
        if (!isDragging) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    BasicTextField(
        state = state,
        placeholder = R.string.enter_subtask_name,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.DragIndicator,
                contentDescription = null,
                modifier = Modifier.detectReorderAfterLongPress(reorderableState)
            )
        },
        trailingIcon = {
            IconButton(onClick = onRemoveSubtask) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.remove_subtask)
                )
            }
        },
        modifier = modifier
    )
}