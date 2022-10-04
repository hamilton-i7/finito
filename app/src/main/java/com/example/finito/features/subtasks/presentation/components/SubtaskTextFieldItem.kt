package com.example.finito.features.subtasks.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.components.textfields.BasicTextField
import com.example.finito.core.presentation.util.SubtaskTextField
import com.example.finito.ui.theme.finitoColors
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress

@Composable
fun SubtaskTextFieldItem(
    state: SubtaskTextField,
    reorderableState: ReorderableLazyListState,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    onRemoveSubtask: () -> Unit = {},
    focusManager: FocusManager = LocalFocusManager.current,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    isDragging: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done
    ),
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    val elevation by animateDpAsState(targetValue = if (isDragging) 3.dp else 0.dp)

    LaunchedEffect(isDragging) {
        if (!isDragging) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        focusManager.clearFocus()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(finitoColors.surfaceColorAtElevation(elevation))
            .then(modifier)
    ) {
        IconButton(
            onClick = {},
            modifier = Modifier.detectReorderAfterLongPress(reorderableState)
        ) {
            Icon(
                imageVector = Icons.Outlined.DragIndicator,
                contentDescription = null
            )
        }
        BasicTextField(
            state = state.toTextFieldState(),
            placeholder = R.string.enter_subtask_name,
            trailingIcon = {
                IconButton(onClick = onRemoveSubtask) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(id = R.string.remove_subtask)
                    )
                }
            },
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = textFieldModifier
        )
    }
}