package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.util.menu.BoardCardMenuOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.labels.domain.entity.SimpleLabel
import org.burnoutcrew.reorderable.ReorderableLazyGridState
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
fun BoardLayout(
    reorderableListState: ReorderableLazyListState = rememberReorderableLazyListState(
    onMove = { _, _ -> }
    ),
    reorderableGridState: ReorderableLazyGridState = rememberReorderableLazyGridState(
        onMove = { _, _ -> }
    ),
    gridLayout: Boolean = true,
    labels: List<SimpleLabel> = emptyList(),
    labelFilters: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {},
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    sortingOptions: List<SortingOption.Common> = emptyList(),
    selectedSortingOption: SortingOption.Common? = null,
    onSortOptionClick: (option: SortingOption.Common) -> Unit = {},
    onBoardClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean,
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<BoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit,
    onMenuItemClick: (board: BoardWithLabelsAndTasks, option: BoardCardMenuOption) -> Unit
) {
    val contentPadding = PaddingValues(
        vertical = 12.dp,
        horizontal = 8.dp
    )

    if (gridLayout) {
        BoardsGrid(
            contentPadding = contentPadding,
            reorderableState = reorderableGridState,
            labels = labels,
            labelFilters = labelFilters,
            onLabelClick = onLabelClick,
            onRemoveFiltersClick = onRemoveFiltersClick,
            sortingOptions = sortingOptions,
            selectedSortingOption = selectedSortingOption,
            onSortOptionClick = onSortOptionClick,
            boards = boards,
            onBoardClick = onBoardClick,
            showCardMenu = showCardMenu,
            onDismissMenu = onDismissMenu,
            options = options,
            onCardOptionsClick = onCardOptionsClick,
            onMenuItemClick = onMenuItemClick
        )
    } else {
        BoardsList(
            contentPadding = contentPadding,
            reorderableState = reorderableListState,
            labels = labels,
            labelFilters = labelFilters,
            onLabelClick = onLabelClick,
            onRemoveFiltersClick = onRemoveFiltersClick,
            sortingOptions = sortingOptions,
            selectedSortingOption = selectedSortingOption,
            onSortOptionClick = onSortOptionClick,
            boards = boards,
            onBoardClick = onBoardClick,
            showCardMenu = showCardMenu,
            onDismissMenu = onDismissMenu,
            options = options,
            onCardOptionsClick = onCardOptionsClick,
            onMenuItemClick = onMenuItemClick
        )
    }
}