package com.example.finito.features.tasks.presentation.screen.addedittask.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.domain.Reminder
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.components.textfields.ClickableTextField
import com.example.finito.core.presentation.util.menu.TaskReminderOption

private val menuOptions = listOf(
    TaskReminderOption.FiveMinutes,
    TaskReminderOption.TenMinutes,
    TaskReminderOption.FifteenMinutes,
    TaskReminderOption.ThirtyMinutes,
)

@Composable
fun ReminderDropdownTextField(
    selectedReminder: Reminder? = null,
    onReminderClick: () -> Unit = {},
    enabled: Boolean = true,
    showDropdown: Boolean = false,
    onDismissDropdown: () -> Unit = {},
    onOptionClick: (TaskReminderOption) -> Unit = {},
) {
    val degrees: Float by animateFloatAsState(if (showDropdown) -180f else 0f)

    Box {
        ClickableTextField(
            onClick = onReminderClick,
            value = selectedReminder?.label?.let { stringResource(id = it) } ?: "",
            enabled = enabled,
            placeholder = { Text(text = stringResource(id = R.string.reminder)) },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.NotificationAdd, contentDescription = null)
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = stringResource(
                        id = if (showDropdown) R.string.hide_reminders else R.string.show_reminders
                    ),
                    modifier = Modifier.rotate(degrees)
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        FinitoMenu(
            show = showDropdown,
            onDismiss = onDismissDropdown,
            options = menuOptions,
            onOptionClick = onOptionClick
        )
    }
}