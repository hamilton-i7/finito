package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finito.core.domain.util.BoardCardMenuOption
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
    showCardMenu: (boardId: Int) -> Boolean,
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<BoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit,
    onMenuItemClick: (board: BoardWithLabelsAndTasks, option: BoardCardMenuOption) -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (labels.isNotEmpty()) {
            item(contentType = ContentType.LABEL_FILTERS) {
                LabelChips(
                    labels,
                    selectedLabels = labelFilters,
                    onLabelClick = onLabelClick,
                    onRemoveFiltersClick = onRemoveFiltersClick,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        if (sortingOptions.isNotEmpty()) {
            item(contentType = ContentType.SORTING_OPTIONS) {
                SortingChips(
                    options = sortingOptions,
                    selectedOption = selectedSortingOption,
                    onOptionClick = {
                        onSortOptionClick(it as SortingOption.Common)
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
        items(boards) {
            BoardCard(
                onClick = { onBoardClick(it.board.boardId) },
                board = it,
                onOptionsClick = { onCardOptionsClick(it.board.boardId) },
                showMenu = showCardMenu(it.board.boardId),
                onDismissMenu = { onDismissMenu(it.board.boardId) },
                options = options,
                onMenuItemClick = { option -> onMenuItemClick(it, option) }
            )
        }
    }
}