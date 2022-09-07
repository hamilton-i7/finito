package com.example.finito.features.boards.presentation.archive

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
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.components.bars.SmallTopBar
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
fun ArchiveScreen(
    navHostController: NavHostController,
    drawerState: DrawerState,
    archiveViewModel: ArchiveViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val simpleTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val keyboardVisible = WindowInsets.isImeVisible

    Scaffold(
        topBar = {
            if (archiveViewModel.showSearchBar) {
                SearchTopBar(
                    query = archiveViewModel.searchQuery,
                    onQueryChange = {
                        archiveViewModel.onEvent(ArchiveEvent.SearchBoards(it))
                    },
                    onBackClick = {
                        archiveViewModel.onEvent(ArchiveEvent.ShowSearchBar(show = false))
                    },
                    scrollBehavior = searchTopBarScrollBehavior,
                )
            } else {
                SmallTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    title = R.string.archive,
                    scrollBehavior = simpleTopBarScrollBehavior
                )
            }
        },
        bottomBar = if (!keyboardVisible) {
            {
                BottomBar(
                    fabDescription = R.string.add_board,
                    searchDescription = R.string.search_boards,
                    onChangeLayoutClick = {
                        archiveViewModel.onEvent(ArchiveEvent.ToggleLayout)
                    },
                    gridLayout = archiveViewModel.gridLayout,
                    onSearchClick = {
                        archiveViewModel.onEvent(ArchiveEvent.ShowSearchBar(show = true))
                    }
                )
            }
        } else {{}},
        modifier = Modifier.nestedScroll(
            if (archiveViewModel.showSearchBar)
                searchTopBarScrollBehavior.nestedScrollConnection
            else
                simpleTopBarScrollBehavior.nestedScrollConnection
        )
    ) { innerPadding ->
        ArchiveScreen(
            paddingValues = innerPadding,
            gridLayout = archiveViewModel.gridLayout,
            labels = archiveViewModel.labels,
            labelFilters = archiveViewModel.labelFilters,
            onLabelClick = {
                archiveViewModel.onEvent(ArchiveEvent.AddFilter(it))
            },
            onRemoveFiltersClick = {
                archiveViewModel.onEvent(ArchiveEvent.RemoveFilters)
            },
            boards = archiveViewModel.boards,
            selectedSortingOption = archiveViewModel.boardsOrder,
            onSortOptionClick = {
                archiveViewModel.onEvent(ArchiveEvent.SortBoards(it))
            },
        )
    }
}

@Composable
private fun ArchiveScreen(
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
private fun ArchiveScreenPreview() {
    FinitoTheme {
        Surface {
            ArchiveScreen(
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
private fun ArchiveScreenPreviewDark() {
    FinitoTheme {
        Surface {
            ArchiveScreen(
                labels = SimpleLabel.dummyLabels,
                boards = BoardWithLabelsAndTasks.dummyBoards,
                selectedSortingOption = SortingOption.Common.Newest
            )
        }
    }
}