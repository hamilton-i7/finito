package com.example.finito.core.presentation.components.textfields

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.Priority
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityChips(
    modifier: Modifier = Modifier,
    selectedPriority: Priority?,
    onPriorityClick: (Priority) -> Unit = {}
) {
    val containerColor = when (selectedPriority) {
        Priority.LOW -> finitoColors.lowPriorityContainer
        Priority.MEDIUM -> finitoColors.mediumPriorityContainer
        else -> finitoColors.urgentPriorityContainer
    }
    val onContainerColor = when (selectedPriority) {
        Priority.LOW -> finitoColors.onLowPriorityContainer
        Priority.MEDIUM -> finitoColors.onMediumPriorityContainer
        else -> finitoColors.onUrgentPriorityContainer
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(id = R.string.priority),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.values().forEach { priority ->
                val selected = priority.label == selectedPriority?.label
                FilterChip(
                    selected = selected,
                    onClick = { onPriorityClick(priority) },
                    leadingIcon = leadingIcon@{
                        if (!selected) return@leadingIcon
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    label = { Text(stringResource(id = priority.label)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = containerColor,
                        selectedLabelColor = onContainerColor,
                        selectedLeadingIconColor = onContainerColor
                    ),
                    modifier = Modifier
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 100)
                        )
                )
            }
        }
    }
}