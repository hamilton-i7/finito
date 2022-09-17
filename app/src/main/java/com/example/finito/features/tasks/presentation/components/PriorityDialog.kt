package com.example.finito.features.tasks.presentation.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.finito.R
import com.example.finito.core.domain.Priority
import com.example.finito.core.presentation.components.FinitoDivider
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@Composable
fun PriorityDialog(
    onDismiss: () -> Unit,
    selectedPriority: Priority?,
    onPrioritySelect: (Priority?) -> Unit,
    onDismissClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = finitoColors.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = stringResource(id = R.string.choose_priority),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(24.dp)
            )
            FinitoDivider()
            Column(
                modifier = Modifier
                    .heightIn(
                        max = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                            150.dp
                        else
                            Dp.Unspecified
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                PriorityItem(
                    priority = null,
                    onClick = onPrioritySelect,
                    selected = selectedPriority == null,
                    label = R.string.none
                )
                enumValues<Priority>().forEach {
                    PriorityItem(
                        priority = it,
                        onClick = onPrioritySelect,
                        selected = selectedPriority == it,
                        label = it.label
                    )
                }
            }
            FinitoDivider()
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                TextButton(onClick = onDismissClick) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = {
                        onConfirmClick()
                        onDismiss()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = finitoColors.primaryContainer,
                        contentColor = finitoColors.onPrimaryContainer
                    )
                ) { Text(text = stringResource(id = R.string.confirm)) }
            }
        }
    }
}

@Composable
private fun PriorityItem(
    priority: Priority?,
    onClick: (Priority?) -> Unit,
    selected:  Boolean,
    @StringRes label: Int,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { onClick(priority) }
            )
            .padding(horizontal = 24.dp, vertical = 2.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = { onClick(priority) }
        )
        Text(
            text = stringResource(id = label),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@ThemePreviews
@Composable
private fun PriorityDialogPreview() {
    FinitoTheme {
        Surface {
            PriorityDialog(
                onDismiss = {},
                selectedPriority = Priority.URGENT,
                onPrioritySelect = {}
            )
        }
    }
}