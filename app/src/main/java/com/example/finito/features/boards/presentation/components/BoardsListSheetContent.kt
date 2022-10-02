package com.example.finito.features.boards.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardsListSheetContent(
    boards: List<SimpleBoard>,
    selectedBoard: SimpleBoard? = null,
    onBoardClick: (SimpleBoard) -> Unit = {},
    state: LazyListState = rememberLazyListState(),
) {
    Surface(tonalElevation = 1.dp) {
        LazyColumn(
            state = state,
            modifier = Modifier.systemBarsPadding(),
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.move_to),
                    style = MaterialTheme.typography.bodySmall,
                    color = finitoColors.outline,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(boards) { board ->
                val selected = board.boardId == selectedBoard?.boardId
                ListItem(
                    headlineText = {
                        Text(
                            text = board.name,
                            color = if (selected) finitoColors.primary else finitoColors.onSurface
                        )
                    },
                    trailingContent = trailingContent@{
                        if (!selected) return@trailingContent
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(id = R.string.selected),
                            tint = finitoColors.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBoardClick(board) }
                )
            }
        }
    }
}