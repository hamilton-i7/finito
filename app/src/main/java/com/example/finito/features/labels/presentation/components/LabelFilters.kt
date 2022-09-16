package com.example.finito.features.labels.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.features.labels.domain.entity.SimpleLabel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelFilters(
    labels: List<SimpleLabel>,
    modifier: Modifier = Modifier,
    selectedLabels: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {},
) {
    val labelIds = selectedLabels.groupBy { it }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(id = R.string.filter_by_label),
            style = MaterialTheme.typography.bodyMedium
        )
        LazyRow(state = listState) {
            item {
                AnimatedVisibility(
                    visible = selectedLabels.isNotEmpty(),
                    enter = slideInHorizontally() + expandHorizontally(),
                    exit = slideOutHorizontally() + shrinkHorizontally(),
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
                        label = { Text(text = stringResource(id = R.string.remove_filters)) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            itemsIndexed(labels) { index, label ->
                val selected = labelIds[label.labelId] != null
                FilterChip(
                    selected = selected,
                    onClick = onLabelClick@{
                        onLabelClick(label.labelId)
                        // Only scroll to remove filters if the user is
                        // near the beginning of the list
                        with(listState) {
                            if (index >= 3 || selectedLabels.isNotEmpty() || isScrollInProgress) {
                                return@onLabelClick
                            }
                            scope.launch { animateScrollToItem(index = 0) }
                        }
                    },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null,
                    label = { Text(label.name) },
                    modifier = Modifier.padding(
                        start = if (index == 0) 0.dp else 4.dp,
                        end = if (index == labels.lastIndex) 0.dp else 4.dp
                    )
                )
            }
        }
    }
}