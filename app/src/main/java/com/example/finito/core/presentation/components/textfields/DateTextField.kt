package com.example.finito.core.presentation.components.textfields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@Composable
fun DateTextField(
    date: String,
    onDateRemove: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    ClickableTextField(
        onClick = onClick,
        value = date,
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onDateRemove) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.remove_date)
                )
            }
        },
        placeholder = { Text(text = stringResource(id = R.string.date)) },
    )
}