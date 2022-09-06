package com.example.finito.features.labels.presentation.components

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

    LazyRow {
        if (selectedLabels.isNotEmpty()) {
            item {
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
            FilterChip(
                selected = labelIds[label.labelId] != null,
                onClick = { onLabelClick(label.labelId) },
                leadingIcon = if (labelIds[label.labelId] != null) {
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