package com.example.finito.core.presentation.components.textfields

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.ui.theme.DisabledAlpha
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicTextField(
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int? = null,
) {
    val (value, onValueChange) = textFieldState

    TextField(
        value = value,
        onValueChange = onValueChange,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            placeholderColor = finitoColors.onSurface.copy(alpha = DisabledAlpha)
        ),
        placeholder = placeholderContent@{
            if (placeholder == null) return@placeholderContent
            Text(text = stringResource(id = placeholder))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        modifier = modifier.fillMaxWidth()
    )
}