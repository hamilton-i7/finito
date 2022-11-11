package com.example.finito.features.boards.presentation.screen.searchboard

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.util.normalize
import com.example.finito.core.presentation.components.EmptyContent
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.header
import com.example.finito.core.presentation.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.screen.searchboard.components.SelectModeTopBar
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.SelectableLabel
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBoardScreen(
    searchBoardViewModel: SearchBoardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToBoard: (boardId: Int) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var performedFeedback by rememberSaveable { mutableStateOf(false) }
    var selectedLabelsAmount by remember { mutableStateOf(0) }
    val selectModeEnabled = searchBoardViewModel.mode == SearchBoardEvent.Mode.SELECT

    val gridState = rememberLazyGridState()

    BackHandler {
        if (searchBoardViewModel.mode == SearchBoardEvent.Mode.SELECT
            || searchBoardViewModel.mode == SearchBoardEvent.Mode.SEARCH) {
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

    LaunchedEffect(searchBoardViewModel.labelFilters) {
        if (searchBoardViewModel.labelFilters.isEmpty()) return@LaunchedEffect
        selectedLabelsAmount = searchBoardViewModel.labelFilters.size
    }

    Scaffold(
        topBar = {
            Crossfade(targetState = selectModeEnabled) { enabled ->
                if (enabled) {
                    SelectModeTopBar(
                        selectedAmount = selectedLabelsAmount,
                        scrollBehavior = topBarScrollBehavior,
                        onBackClick = {
                            searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                                mode = SearchBoardEvent.Mode.IDLE
                            ))
                        },
                        onConfirmClick = {
                            searchBoardViewModel.onEvent(SearchBoardEvent.SearchBoards(query = ""))
                            searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                                mode = SearchBoardEvent.Mode.SEARCH
                            ))
                        },
                    )
                    return@Crossfade
                }

                val placeholder = when {
                    searchBoardViewModel.mode == SearchBoardEvent.Mode.SEARCH
                            && searchBoardViewModel.labelFilters.isNotEmpty() -> {
                        val labelNames = searchBoardViewModel.labelFilters.joinToString {
                            "\"${it.name}\""
                        }
                        "${stringResource(id = R.string.search_in)} $labelNames"
                    }
                    else -> stringResource(id = R.string.search_boards)
                }

                SearchTopBar(
                    queryState = searchBoardViewModel.searchQueryState.copy(
                        onValueChange = {
                            searchBoardViewModel.onEvent(SearchBoardEvent.SearchBoards(it))
                        }
                    ),
                    placeholder = placeholder,
                    onBackClick = onBackClick@{
                        if (searchBoardViewModel.mode == SearchBoardEvent.Mode.SEARCH) {
                            searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                                mode = SearchBoardEvent.Mode.IDLE
                            ))
                            return@onBackClick
                        }
                        onNavigateBack()
                    },
                    scrollBehavior = topBarScrollBehavior,
                )
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
            showNoResults = searchBoardViewModel.showNoResults,
            gridState = gridState,
            gridLayout = searchBoardViewModel.gridLayout,
            boards = searchBoardViewModel.boards,
            onBoardClick = onNavigateToBoard,
            onEnableSearchMode = {
                searchBoardViewModel.onEvent(SearchBoardEvent.SelectLabel(it))
                searchBoardViewModel.onEvent(SearchBoardEvent.SearchBoards(query = ""))
                searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                    mode = SearchBoardEvent.Mode.SEARCH
                ))
            },
            onEnableSelectMode = {
                searchBoardViewModel.onEvent(SearchBoardEvent.SelectLabel(it))
                searchBoardViewModel.onEvent(SearchBoardEvent.ChangeMode(
                    mode = SearchBoardEvent.Mode.SELECT
                ))
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
    gridState: LazyGridState = rememberLazyGridState(),
    gridLayout: Boolean = true,
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

    LaunchedEffect(boards) { gridState.scrollToItem(index = 0) }

    Surface(modifier = Modifier
        .imePadding()
        .padding(paddingValues)) {
        when (mode) {
            SearchBoardEvent.Mode.IDLE, SearchBoardEvent.Mode.SELECT -> {
                if (labels.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        EmptyContent(icon = R.drawable.color_search, title = R.string.find_boards)
                    }
                    return@Surface
                }
                val grouped = labels.groupBy { it.name[0].normalize().uppercase() }

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
                                modifier = Modifier.padding(
                                    start = 8.dp,
                                    top = 32.dp,
                                    bottom = 4.dp
                                )
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
            SearchBoardEvent.Mode.SEARCH -> {
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
                BoardLayout(
                    gridLayout = gridLayout,
                    gridState = gridState,
                    boards = boards,
                    onBoardClick = onBoardClick,
                    showCardMenu = showCardMenu,
                    onCardOptionsClick = onCardOptionsClick,
                    onDismissMenu = onDismissMenu,
                    options = listOf(
                        ActiveBoardCardMenuOption.Archive,
                        ActiveBoardCardMenuOption.Delete,
                    ),
                    onMenuItemClick = { boardId, option ->
                        onMenuItemClick(boardId, option as ActiveBoardCardMenuOption)
                    },
                )
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