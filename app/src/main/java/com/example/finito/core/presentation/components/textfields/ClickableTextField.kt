package com.example.finito.core.presentation.components.textfields

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun ClickableTextField(
    onClick: () -> Unit,
    value: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
) {
    FinitoTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        label = label,
        placeholder = placeholder,
        enabled = false,
        colors = FinitoTextFieldDefaults.clickableTextFieldColors(),
        modifier = modifier
            .clip(FinitoTextFieldDefaults.Shape)
            .clickable(onClick = onClick),
    )
}