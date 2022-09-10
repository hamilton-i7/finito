package com.example.finito.core.presentation.components.textfields

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinitoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    colors: TextFieldColors = FinitoTextFieldDefaults.textFieldColors(),
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        singleLine = singleLine,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = FinitoTextFieldDefaults.Shape,
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = finitoColors.outline,
                shape = FinitoTextFieldDefaults.Shape
            ),
    )
}

object FinitoTextFieldDefaults {
    val Shape = RoundedCornerShape(16.dp)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun textFieldColors() = TextFieldDefaults.textFieldColors(
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        containerColor = finitoColors.surfaceColorAtElevation(1.dp),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun clickableTextFieldColors(enabled: Boolean = true): TextFieldColors {
        return if (enabled) {
            TextFieldDefaults.textFieldColors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                containerColor = finitoColors.surfaceColorAtElevation(1.dp),
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = finitoColors.onSurface,
                disabledLeadingIconColor = finitoColors.onSurfaceVariant,
                disabledTrailingIconColor = finitoColors.onSurfaceVariant,
            )
        } else {
            val disabledAlpha = 0.38f
            TextFieldDefaults.textFieldColors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                containerColor = finitoColors.surfaceColorAtElevation(1.dp),
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = finitoColors.onSurface.copy(alpha = disabledAlpha),
                disabledLeadingIconColor = finitoColors.onSurfaceVariant.copy(alpha = disabledAlpha),
                disabledTrailingIconColor = finitoColors.onSurfaceVariant.copy(alpha = disabledAlpha),
            )
        }
    }
}