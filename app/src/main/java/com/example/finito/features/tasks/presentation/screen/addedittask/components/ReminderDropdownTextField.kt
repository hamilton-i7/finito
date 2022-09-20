package com.example.finito.features.tasks.presentation.screen.addedittask.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.domain.Reminder
import com.example.finito.core.presentation.components.textfields.ClickableTextField
import com.example.finito.core.presentation.util.menu.TaskReminderOption

private val menuOptions = listOf(
    TaskReminderOption.FiveMinutes,
    TaskReminderOption.TenMinutes,
    TaskReminderOption.FifteenMinutes,
    TaskReminderOption.ThirtyMinutes,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDropdownTextField(
    selectedReminder: Reminder? = null,
    onReminderClick: () -> Unit = {},
    enabled: Boolean = true,
    showDropdown: Boolean = false,
    onDismissDropdown: () -> Unit = {},
    onOptionClick: (TaskReminderOption) -> Unit = {},
) {
    ExposedDropdownMenuBox(
        expanded = showDropdown,
        onExpandedChange = {}
    ) {
        ClickableTextField(
            onClick = onReminderClick,
            value = selectedReminder?.label?.let { stringResource(id = it) } ?: "",
            enabled = enabled,
            placeholder = { Text(text = stringResource(id = R.string.reminder)) },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.NotificationAdd, contentDescription = null)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = onDismissDropdown,
            modifier = Modifier.exposedDropdownSize()
        ) {
            menuOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = option.label)) },
                    onClick = { onOptionClick(option) }
                )
            }
        }
    }
}