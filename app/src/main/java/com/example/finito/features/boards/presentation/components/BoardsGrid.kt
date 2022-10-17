package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.components.CommonSortingIndicator
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.menu.BoardCardMenuOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.utils.BOARD_COLUMNS
import org.burnoutcrew.reorderable.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardsGrid(
    contentPadding: PaddingValues,
    reorderableState: ReorderableLazyGridState = rememberReorderableLazyGridState(
        onMove = { _, _ -> }
    ),
    allowDrag: Boolean = false,
    selectedSortingOption: SortingOption.Common? = null,
    onSortOptionClick: () -> Unit = {},
    boards: List<BoardWithLabelsAndTasks>,
    onBoardClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean,
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<BoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit,
    onMenuItemClick: (board: BoardWithLabelsAndTasks, option: BoardCardMenuOption) -> Unit
) {
    LazyVerticalGrid(
        state = reorderableState.gridState,
        columns = GridCells.Fixed(count = BOARD_COLUMNS),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(reorderableState)
    ) {
        if (selectedSortingOption != null) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = ContentTypes.SORTING_OPTIONS) {
                Column {
                    CommonSortingIndicator(
                        option = selectedSortingOption,
                        onClick = onSortOptionClick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        items(boards, key = { it.board.boardId }, contentType = { ContentTypes.BOARDS }) {
            ReorderableItem(
                reorderableState,
                key = it.board.boardId,
                orientationLocked = false,
                defaultDraggingModifier = Modifier.animateItemPlacement()
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