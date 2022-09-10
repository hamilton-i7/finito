package com.example.finito.core.presentation.components.textfields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@Composable
fun TimeTextField(
    time: String,
    onTimeRemove: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    ClickableTextField(
        onClick = onClick,
        value = time,
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onTimeRemove) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.remove_time)
                )
            }
        },
        placeholder = { Text(text = stringResource(id = R.string.time)) },
    )
}