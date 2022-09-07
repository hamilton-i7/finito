package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.components.SortingChips
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.utils.ContentType
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelChips

@Composable
fun BoardsList(
    contentPadding: PaddingValues,
    labels: List<SimpleLabel> = emptyList(),
    labelFilters: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {},
    sortingOptions: List<SortingOption.Common> = emptyList(),
    selectedSortingOption: SortingOption.Common = SortingOption.Common.Newest,
    onSortOptionClick: (option: SortingOption.Common) -> Unit = {},
    boards: List<BoardWithLabelsAndTasks>,
    onBoardClick: (boardId: Int) -> Unit = {},
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (labels.isNotEmpty()) {
            item(contentType = ContentType.LABEL_FILTERS) {
                Column {
                    Text(
                        text = stringResource(id = R.string.filter_by_label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LabelChips(
                        labels,
                        selectedLabels = labelFilters,
                        onLabelClick = onLabelClick,
                        onRemoveFiltersClick = onRemoveFiltersClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        if (sortingOptions.isNotEmpty()) {
            item(contentType = ContentType.SORTING_OPTIONS) {
                Column {
                    Text(
                        text = stringResource(id = R.string.sort_by),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SortingChips(
                        options = sortingOptions,
                        selectedOption = selectedSortingOption,
                        onOptionClick = {
                            onSortOptionClick(it as SortingOption.Common)
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        items(boards) {
            BoardCard(onClick = { onBoardClick(it.board.boardId) }, board = it)
        }
    }
}