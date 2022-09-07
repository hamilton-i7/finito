package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.finito.features.boards.utils.BOARD_COLUMNS
import com.example.finito.features.boards.utils.ContentType
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelChips

@Composable
fun BoardsGrid(
    contentPadding: PaddingValues,
    labels: List<SimpleLabel>,
    labelFilters: List<Int>,
    onLabelClick: (labelId: Int) -> Unit,
    onRemoveFiltersClick: () -> Unit,
    sortingOptions: List<SortingOption.Common>,
    selectedSortingOption: SortingOption.Common,
    onSortOptionClick: (option: SortingOption.Common) -> Unit,
    boards: List<BoardWithLabelsAndTasks>,
    onBoardClick: (boardId: Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = BOARD_COLUMNS),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        if (labels.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = ContentType.LABEL_FILTERS) {
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
        item(span = { GridItemSpan(maxLineSpan) }, contentType = ContentType.SORTING_OPTIONS) {
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
        items(boards) {
            BoardCard(onClick = { onBoardClick(it.board.boardId) }, board = it)
        }
    }
}