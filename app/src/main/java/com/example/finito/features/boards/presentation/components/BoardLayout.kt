package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.util.menu.BoardCardMenuOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import org.burnoutcrew.reorderable.ReorderableLazyGridState
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
fun BoardLayout(
    reorderableListState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    listState: LazyListState? = null,
    reorderableGridState: ReorderableLazyGridState = rememberReorderableLazyGridState(
        onMove = { _, _ -> }
    ),
    gridState: LazyGridState? = null,
    allowDrag: Boolean = false,
    gridLayout: Boolean = true,
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    selectedSortingOption: SortingOption.Common? = null,
    onSortOptionClick: () -> Unit = {},
    onBoardClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean = { false },
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<BoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit = {},
    onMenuItemClick: (
        board: BoardWithLabelsAndTasks,
        option: BoardCardMenuOption,
    ) -> Unit = {_, _ -> },
) {
    val contentPadding = PaddingValues(12.dp)

    if (gridLayout) {
        BoardsGrid(
            contentPadding = contentPadding,
            reorderableState = reorderableGridState,
            gridState = gridState,
            allowDrag = allowDrag,
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
            allowDrag = allowDrag,
            reorderableState = reorderableListState,
            listState = listState,
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