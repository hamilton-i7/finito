package com.example.finito.features.boards.presentation.screen.searchboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.components.EmptyContent
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardCard
import com.example.finito.features.boards.presentation.screen.searchboard.components.SelectModeTopBar
import com.example.finito.features.boards.utils.BOARD_COLUMNS
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.SelectableLabel
import com.example.finito.ui.theme.FinitoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBoardScreen(
    searchBoardViewModel: SearchBoardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler {
        if (searchBoardViewModel.mode == SearchBoardEvent.Mode.SELECT) {
            searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                mode = SearchBoardEvent.Mode.IDLE
            ))
        }
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            Crossfade(targetState = searchBoardViewModel.mode) { mode ->
                when (mode) {
                    SearchBoardEvent.Mode.SELECT -> {
                        SelectModeTopBar(
                            selectedAmount = searchBoardViewModel.labelFilters.size,
                            scrollBehavior = topBarScrollBehavior,
                            onBackClick = {
                                searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                                    mode = SearchBoardEvent.Mode.IDLE
                                ))
                            },
                            onConfirmClick = {
                                searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                                    mode = SearchBoardEvent.Mode.SEARCH
                                ))
                            },
                        )
                    }
                    SearchBoardEvent.Mode.IDLE, SearchBoardEvent.Mode.SEARCH -> {
                        SearchTopBar(
                            queryState = searchBoardViewModel.searchQueryState.copy(
                                onValueChange = {
                                    searchBoardViewModel.onEvent(SearchBoardEvent.SearchBoards(it))
                                }
                            ),
                            scrollBehavior = topBarScrollBehavior
                        )
                    }
                }
            }
        },
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        SearchBoardScreen(
            paddingValues = innerPadding,
            mode = searchBoardViewModel.mode,
            labels = searchBoardViewModel.labels,
            selectedLabels = searchBoardViewModel.labelFilters,
            boards = searchBoardViewModel.boards
        )
    }
}

@Composable
private fun SearchBoardScreen(
    paddingValues: PaddingValues = PaddingValues(),
    mode: SearchBoardEvent.Mode = SearchBoardEvent.Mode.IDLE,
    showNoResults: Boolean = false,
    labels: List<SimpleLabel> = emptyList(),
    selectedLabels: List<SimpleLabel> = emptyList(),
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    onBoardClick: (boardId: Int) -> Unit = {},
    onCardOptionsClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean = { false },
    onDismissMenu: (boardId: Int) -> Unit = {},
    onMenuItemClick: (
        board: BoardWithLabelsAndTasks,
        option: ActiveBoardCardMenuOption,
    ) -> Unit = { _, _ -> }
) {
    val selectedLabelsMap = selectedLabels.groupBy { it.labelId }
    val options = listOf(
        ActiveBoardCardMenuOption.Archive,
        ActiveBoardCardMenuOption.Delete,
    )

    Surface(modifier = Modifier.padding(paddingValues)) {
        when (mode) {
            SearchBoardEvent.Mode.IDLE, SearchBoardEvent.Mode.SELECT -> {
                if (labels.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        EmptyContent(icon = R.drawable.color_search, title = R.string.find_boards)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 96.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(labels) { label ->
                            SelectableLabel(
                                label,
                                selected = selectedLabelsMap[label.labelId] != null,
                            )
                        }
                    }
                }
            }
            SearchBoardEvent.Mode.SEARCH -> {
                if (boards.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        EmptyContent(
                            icon = R.drawable.todo_list,
                            title = R.string.find_boards
                        )
                    }
                    return@Surface
                }
                if (showNoResults) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        EmptyContent(
                            icon = R.drawable.nothing_found,
                            title = R.string.no_boards_match
                        )
                    }
                    return@Surface
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(count = BOARD_COLUMNS),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(boards) {
                        BoardCard(
                            onClick = { onBoardClick(it.board.boardId) },
                            board = it,
                            onOptionsClick = { onCardOptionsClick(it.board.boardId) },
                            showMenu = showCardMenu(it.board.boardId),
                            onDismissMenu = { onDismissMenu(it.board.boardId) },
                            options = options,
                            onMenuItemClick = { option ->
                                onMenuItemClick(it, option as ActiveBoardCardMenuOption)
                            },
                        )
                    }
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun SearchBoardScreenPreview() {
    FinitoTheme {
        Surface {
            SearchBoardScreen(
                labels = SimpleLabel.dummyLabels,
                selectedLabels = SimpleLabel.dummyLabels.filter { it.labelId % 5 == 0 }
            )
        }
    }
}

@CompletePreviews
@Composable
private fun SearchBoardScreenEmptyPreview() {
    FinitoTheme {
        Surface {
            SearchBoardScreen(labels = emptyList())
        }
    }
}