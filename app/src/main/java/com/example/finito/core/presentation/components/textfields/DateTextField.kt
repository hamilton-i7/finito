package com.example.finito.core.presentation.components.textfields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.features.tasks.domain.util.isCurrentYear
import com.example.finito.features.tasks.domain.util.toCurrentYearFormat
import com.example.finito.features.tasks.domain.util.toFullFormat
import java.time.LocalDate

@Composable
fun DateTextField(
    date: LocalDate?,
    modifier: Modifier = Modifier,
    onDateRemove: () -> Unit = {},
    onClick: () -> Unit = {},
    enabled: Boolean = true,
) {
    val locale = LocalConfiguration.current.locales[0]

    val formattedDate = date?.let {
        val today = LocalDate.now()
        if (it.isCurrentYear(today)) {
            it.toCurrentYearFormat(locale, complete = true)
        } else {
            it.toFullFormat(locale, complete = true)
        }
    } ?: ""

    ClickableTextField(
        onClick = onClick,
        value = formattedDate,
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onDateRemove, enabled = enabled) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.remove_date)
                )
            }
        },
        placeholder = { Text(text = stringResource(id = R.string.date)) },
        enabled = enabled,
        modifier = modifier
    )
}