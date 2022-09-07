package com.example.finito.features.boards.presentation.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.components.SortingChips
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.HomeTopBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardCard
import com.example.finito.features.boards.utils.BOARD_COLUMNS
import com.example.finito.features.boards.utils.ContentType
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.presentation.components.LabelChips
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    drawerState: DrawerState,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val homeTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val keyboardVisible = WindowInsets.isImeVisible

    Scaffold(
        topBar = {
            if (homeViewModel.showSearchBar) {
                SearchTopBar(
                    query = homeViewModel.searchQuery,
                    onQueryChange = {
                        homeViewModel.onEvent(HomeEvent.SearchBoards(it))
                    },
                    onBackClick = {
                        homeViewModel.onEvent(HomeEvent.ShowSearchBar(show = false))
                    },
                    scrollBehavior = searchTopBarScrollBehavior,
                )
            } else {
                HomeTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    scrollBehavior = homeTopBarScrollBehavior
                )
            }
        },
        bottomBar = if (!keyboardVisible) {
            {
                BottomBar(
                    fabDescription = R.string.add_board,
                    searchDescription = R.string.search_boards,
                    onChangeLayoutClick = {
                        homeViewModel.onEvent(HomeEvent.ToggleLayout)
                    },
                    gridLayout = homeViewModel.gridLayout,
                    onSearchClick = {
                        homeViewModel.onEvent(HomeEvent.ShowSearchBar(show = true))
                    }
                )
            }
        } else {{}},
        modifier = Modifier.nestedScroll(
            if (homeViewModel.showSearchBar)
                searchTopBarScrollBehavior.nestedScrollConnection
            else
                homeTopBarScrollBehavior.nestedScrollConnection
        )
    ) { innerPadding ->
        HomeScreen(
            paddingValues = innerPadding,
            gridLayout = homeViewModel.gridLayout,
            labels = homeViewModel.labels,
            labelFilters = homeViewModel.labelFilters,
            onLabelClick = {
                homeViewModel.onEvent(HomeEvent.AddFilter(it))
            },
            onRemoveFiltersClick = {
                homeViewModel.onEvent(HomeEvent.RemoveFilters)
            },
            boards = homeViewModel.boards,
            selectedSortingOption = homeViewModel.boardsOrder,
            onSortOptionClick = {
                homeViewModel.onEvent(HomeEvent.SortBoards(it))
            },
        )
    }
}

@Composable
private fun HomeScreen(
    paddingValues: PaddingValues = PaddingValues(),
    gridLayout: Boolean = true,
    labels: List<SimpleLabel> = emptyList(),
    labelFilters: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {},
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    selectedSortingOption: SortingOption.Common = SortingOption.Common.NameAZ,
    onSortOptionClick: (option: SortingOption.Common) -> Unit = {},
    onBoardClick: (boardId: Int) -> Unit = {}
) {
    val sortingOptions = listOf(
        SortingOption.Common.NameAZ,
        SortingOption.Common.NameZA,
        SortingOption.Common.Newest,
        SortingOption.Common.Oldest,
    )
    val contentPadding = PaddingValues(
        vertical = 12.dp,
        horizontal = 16.dp
    )
    Surface(modifier = Modifier.padding(paddingValues)) {
        if (gridLayout) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(count = BOARD_COLUMNS),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                if (labels.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, contentType = ContentType.LABEL_FILTERS) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.filter_by_label),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LabelChips(
                                labels,
                                selectedLabels = labelFilters,
                                onLabelClick = onLabelClick,
                                onRemoveFiltersClick = onRemoveFiltersClick
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }, contentType = ContentType.SORTING_OPTIONS) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.sort_by),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SortingChips(
                            options = sortingOptions,
                            selectedOption = selectedSortingOption,
                            onOptionClick = {
                                onSortOptionClick(it as SortingOption.Common)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                items(boards) {
                    BoardCard(onClick = { onBoardClick(it.board.boardId) }, board = it)
                }
            }
        } else {
            LazyColumn(
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (labels.isNotEmpty()) {
                    item(contentType = ContentType.LABEL_FILTERS) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.filter_by_label),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LabelChips(
                                labels,
                                selectedLabels = labelFilters,
                                onLabelClick = onLabelClick,
                                onRemoveFiltersClick = onRemoveFiltersClick
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                item(contentType = ContentType.SORTING_OPTIONS) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.sort_by),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SortingChips(
                            options = sortingOptions,
                            selectedOption = selectedSortingOption,
                            onOptionClick = {
                                onSortOptionClick(it as SortingOption.Common)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                items(boards) {
                    BoardCard(onClick = { onBoardClick(it.board.boardId) }, board = it)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FinitoTheme {
        Surface {
            HomeScreen(
                labels = SimpleLabel.dummyLabels,
                boards = BoardWithLabelsAndTasks.dummyBoards,
                selectedSortingOption = SortingOption.Common.Newest
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun HomeScreenPreviewDark() {
    FinitoTheme {
        Surface {
            HomeScreen(
                labels = SimpleLabel.dummyLabels,
                boards = BoardWithLabelsAndTasks.dummyBoards,
                selectedSortingOption = SortingOption.Common.Newest
            )
        }
    }
}