package com.example.finito.features.boards.presentation.screen.searchboard

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.components.EmptyContent
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.header
import com.example.finito.core.presentation.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardCard
import com.example.finito.features.boards.presentation.screen.searchboard.components.SelectModeTopBar
import com.example.finito.features.boards.utils.BOARD_COLUMNS
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.SelectableLabel
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBoardScreen(
    searchBoardViewModel: SearchBoardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val hapticFeedback = LocalHapticFeedback.current
    var performedFeedback by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (searchBoardViewModel.mode == SearchBoardEvent.Mode.SELECT) {
            searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                mode = SearchBoardEvent.Mode.IDLE
            ))
            return@BackHandler
        }
        onNavigateBack()
    }

    LaunchedEffect(searchBoardViewModel.mode) {
        if (searchBoardViewModel.mode != SearchBoardEvent.Mode.SELECT) {
            performedFeedback = false
            return@LaunchedEffect
        }
        if (performedFeedback) return@LaunchedEffect

        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        performedFeedback = true
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
                            scrollBehavior = topBarScrollBehavior,
                            onBackClick = onNavigateBack
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .noRippleClickable { focusManager.clearFocus() }
    ) { innerPadding ->
        SearchBoardScreen(
            paddingValues = innerPadding,
            mode = searchBoardViewModel.mode,
            labels = searchBoardViewModel.labels,
            selectedLabels = searchBoardViewModel.labelFilters,
            boards = searchBoardViewModel.boards,
            onEnableSearchMode = {
                searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                    mode = SearchBoardEvent.Mode.SEARCH
                ))
                searchBoardViewModel.onEvent(SearchBoardEvent.SelectLabel(it))
            },
            onEnableSelectMode = {
                searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                    mode = SearchBoardEvent.Mode.SELECT
                ))
                searchBoardViewModel.onEvent(SearchBoardEvent.SelectLabel(it))
            },
            onSelectLabel = {
                searchBoardViewModel.onEvent(SearchBoardEvent.SelectLabel(it))
            }
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
    ) -> Unit = { _, _ -> },
    onEnableSelectMode: (SimpleLabel) -> Unit = {},
    onEnableSearchMode: (SimpleLabel) -> Unit = {},
    onSelectLabel: (SimpleLabel) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val minSize = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> 150.dp
        else -> 120.dp
    }
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
                    val grouped = labels.groupBy { it.name[0].uppercase() }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        grouped.forEach { (initial, labelsForInitial) ->
                            header {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = finitoColors.primary,
                                    modifier = Modifier.padding(start = 8.dp, top = 32.dp, bottom = 4.dp)
                                )
                            }
                            items(labelsForInitial) { label ->
                                SelectableLabel(
                                    label,
                                    selected = selectedLabelsMap[label.labelId] != null,
                                    onClick = onClick@{
                                        if (mode == SearchBoardEvent.Mode.SELECT) {
                                            onSelectLabel(label)
                                            return@onClick
                                        }
                                        onEnableSearchMode(label)
                                    },
                                    onLongClick = onLongClick@{
                                        if (mode == SearchBoardEvent.Mode.SELECT) return@onLongClick
                                        onEnableSelectMode(label)
                                    }
                                )
                            }
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