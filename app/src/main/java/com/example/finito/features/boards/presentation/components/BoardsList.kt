package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.components.CommonSortingIndicator
import com.example.finito.core.presentation.util.ContentTypes
import com.example.finito.core.presentation.util.menu.BoardCardMenuOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import org.burnoutcrew.reorderable.*

@Composable
fun BoardsList(
    contentPadding: PaddingValues,
    reorderableState: ReorderableLazyListState = rememberReorderableLazyListState(
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
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = reorderableState.listState,
        modifier = Modifier.reorderable(reorderableState)
    ) {
        if (selectedSortingOption != null) {
            item(contentType = ContentTypes.SORTING_OPTIONS) {
                CommonSortingIndicator(
                    option = selectedSortingOption,
                    onClick = onSortOptionClick
                )
                Spacer(modifier = Modifier.height(8.dp))
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