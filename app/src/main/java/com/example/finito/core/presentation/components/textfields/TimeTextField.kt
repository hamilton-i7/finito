package com.example.finito.core.presentation.components.textfields

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.features.tasks.domain.util.formatted
import java.time.LocalTime

@Composable
fun TimeTextField(
    time: LocalTime?,
    modifier: Modifier = Modifier,
    onTimeRemove: () -> Unit = {},
    onClick: () -> Unit = {},
    enabled: Boolean = true,
) {
    val formattedTime = time?.formatted() ?: ""

    ClickableTextField(
        onClick = onClick,
        value = formattedTime,
        leadingIcon = {
            Icon(painter = painterResource(id = R.drawable.clock), contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onTimeRemove, enabled = enabled) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = stringResource(id = R.string.remove_time)
                )
            }
        },
        placeholder = { Text(text = stringResource(id = R.string.time)) },
        enabled = enabled,
        modifier = modifier
    )
}