package com.example.finito.core.presentation.components.textfields

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinitoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    error: Boolean = false,
    @StringRes errorFeedback: Int? = null,
    colors: TextFieldColors = FinitoTextFieldDefaults.textFieldColors(),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth()
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
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = textFieldModifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = finitoColors.outline,
                    shape = FinitoTextFieldDefaults.Shape
                ),
        )
        Text(
            text = if (errorFeedback != null && error) stringResource(id = errorFeedback) else " ",
            color = finitoColors.error,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

object FinitoTextFieldDefaults {
    val Shape = RoundedCornerShape(16.dp)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun textFieldColors() = TextFieldDefaults.textFieldColors(
        unfocusedIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        containerColor = finitoColors.surfaceColorAtElevation(1.dp),
        unfocusedLabelColor = finitoColors.onSurfaceVariant.copy(alpha = 0.60f)
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