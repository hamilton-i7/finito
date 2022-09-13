package com.example.finito.core.presentation.components.menu

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.finito.core.domain.util.menu.MenuOption
import com.example.finito.core.presentation.MENU_MIN_WIDTH
import com.example.finito.ui.theme.finitoColors

@Composable
fun <M: MenuOption> FinitoMenu(
    show: Boolean,
    onDismiss: () -> Unit,
    options: List<M>,
    onOptionClick: (M) -> Unit,
) {
    DropdownMenu(
        expanded = show,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = MENU_MIN_WIDTH)
    ) {
        options.forEach { option ->
            val interactionSource = remember { MutableInteractionSource() }
            DropdownMenuItem(
                text = { Text(stringResource(id = option.label)) },
                onClick = { onOptionClick(option) },
                interactionSource = interactionSource,
                modifier = Modifier.indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(color = finitoColors.primary.copy(alpha = 0.12f))
                )
            )
        }
    }
}