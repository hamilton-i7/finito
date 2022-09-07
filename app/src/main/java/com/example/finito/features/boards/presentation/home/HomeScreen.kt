package com.example.finito.features.boards.presentation.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.HomeTopBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardsGrid
import com.example.finito.features.boards.presentation.components.BoardsList
import com.example.finito.features.labels.domain.entity.SimpleLabel
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
                    },
                    onFabClick = {
                        // TODO: Add create board functionality
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
        SortingOption.Common.Newest,
        SortingOption.Common.Oldest,
        SortingOption.Common.NameAZ,
        SortingOption.Common.NameZA,
    )
    val contentPadding = PaddingValues(
        vertical = 12.dp,
        horizontal = 16.dp
    )
    Surface(modifier = Modifier.padding(paddingValues)) {
        if (gridLayout) {
            BoardsGrid(
                contentPadding = contentPadding,
                labels = labels,
                labelFilters = labelFilters,
                onLabelClick = onLabelClick,
                onRemoveFiltersClick = onRemoveFiltersClick,
                sortingOptions = sortingOptions,
                selectedSortingOption = selectedSortingOption,
                onSortOptionClick = onSortOptionClick,
                boards = boards,
                onBoardClick = onBoardClick
            )
        } else {
            BoardsList(
                contentPadding = contentPadding,
                labels = labels,
                labelFilters = labelFilters,
                onLabelClick = onLabelClick,
                onRemoveFiltersClick = onRemoveFiltersClick,
                sortingOptions = sortingOptions,
                selectedSortingOption = selectedSortingOption,
                onSortOptionClick = onSortOptionClick,
                boards = boards,
                onBoardClick = onBoardClick
            )
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