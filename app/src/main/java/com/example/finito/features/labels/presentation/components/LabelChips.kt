package com.example.finito.features.labels.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.features.labels.domain.entity.SimpleLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelChips(
    labels: List<SimpleLabel>,
    selectedLabels: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {}
) {
    val labelIds = selectedLabels.groupBy { it }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            AnimatedVisibility(
                visible = selectedLabels.isNotEmpty(),
                enter = slideInHorizontally() + expandHorizontally(),
                exit = slideOutHorizontally() + shrinkHorizontally()
            ) {
                SuggestionChip(
                    onClick = onRemoveFiltersClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                        )
                    },
                    label = { Text(text = stringResource(id = R.string.remove_filters)) }
                )
            }
        }
        items(labels) { label ->
            val selected = labelIds[label.labelId] != null
            FilterChip(
                selected = selected,
                onClick = { onLabelClick(label.labelId) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                label = { Text(label.name) }
            )
        }
    }
}