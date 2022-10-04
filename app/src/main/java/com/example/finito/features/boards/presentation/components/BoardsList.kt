package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.components.SortingChips
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.menu.BoardCardMenuOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelFilters
import org.burnoutcrew.reorderable.*

@Composable
fun BoardsList(
    contentPadding: PaddingValues,
    reorderableState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    allowDrag: Boolean = false,
    labels: List<SimpleLabel> = emptyList(),
    labelFilters: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {},
    sortingOptions: List<SortingOption.Common> = emptyList(),
    selectedSortingOption: SortingOption.Common? = null,
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
        state = reorderableState.listState,
        modifier = Modifier.reorderable(reorderableState)
    ) {
        if (labels.isNotEmpty()) {
            item(contentType = ContentTypes.LABEL_FILTERS) {
                LabelFilters(
                    labels,
                    selectedLabels = labelFilters,
                    onLabelClick = onLabelClick,
                    onRemoveFiltersClick = onRemoveFiltersClick,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        if (sortingOptions.isNotEmpty()) {
            item(contentType = ContentTypes.SORTING_OPTIONS) {
                SortingChips(
                    options = sortingOptions,
                    selectedOption = selectedSortingOption,
                    onOptionClick = onSortOptionClick,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
        items(boards, key = { it.board.boardId }, contentType = { ContentTypes.BOARDS }) {
            ReorderableItem(
                reorderableState,
                key = it.board.boardId,
                orientationLocked = false,
            ) { isDragging ->
                BoardCard(
                    onClick = { onBoardClick(it.board.boardId) },
                    board = it,
                    isDragging = isDragging,
                    onOptionsClick = { onCardOptionsClick(it.board.boardId) },
                    showMenu = showCardMenu(it.board.boardId),
                    onDismissMenu = { onDismissMenu(it.board.boardId) },
                    options = options,
                    onMenuItemClick = { option -> onMenuItemClick(it, option) },
                    modifier = if (allowDrag)
                        Modifier.detectReorderAfterLongPress(reorderableState)
                    else
                        Modifier,
                )
            }
        }
    }
}