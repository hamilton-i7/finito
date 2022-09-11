package com.example.finito.features.boards.presentation.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.domain.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    navController: NavHostController,
    appViewModel: AppViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    showSnackbar: (message: Int, () -> Unit) -> Unit,
) {
    LaunchedEffect(Unit) {
        homeViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is HomeViewModel.Event.ShowSnackbar -> {
                    showSnackbar(event.message) {
                        homeViewModel.onEvent(HomeEvent.RestoreBoard)
                    }
                }
            }
        }
        appViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AppViewModel.Event.SearchBoards -> {
                    homeViewModel.onEvent(HomeEvent.SearchBoards(event.query))
                }
            }
        }
    }

    HomeScreen(
        gridLayout = appViewModel.gridLayout,
        labels = homeViewModel.labels,
        labelFilters = homeViewModel.labelFilters,
        onLabelClick = {
            homeViewModel.onEvent(HomeEvent.SelectFilter(it))
        },
        onRemoveFiltersClick = {
            homeViewModel.onEvent(HomeEvent.RemoveFilters)
        },
        boards = homeViewModel.boards,
        onBoardClick = {
            val route = "${Screen.Board.prefix}/${it}"
            navController.navigate(route)
        },
        selectedSortingOption = homeViewModel.boardsOrder,
        onSortOptionClick = {
            homeViewModel.onEvent(HomeEvent.SortBoards(it))
        },
        onCardOptionsClick = {
            homeViewModel.onEvent(HomeEvent.ShowCardMenu(boardId = it, show = true))
        },
        showCardMenu = { homeViewModel.selectedBoardId == it },
        onDismissMenu = {
            homeViewModel.onEvent(HomeEvent.ShowCardMenu(show = false))
        },
        options = listOf(
            ActiveBoardCardMenuOption.Archive,
            ActiveBoardCardMenuOption.Delete,
        ),
        onMenuItemClick = { board, option ->
            homeViewModel.onEvent(HomeEvent.ShowCardMenu(show = false))
            when (option) {
                ActiveBoardCardMenuOption.Archive -> {
                    homeViewModel.onEvent(HomeEvent.ArchiveBoard(board))
                }
                ActiveBoardCardMenuOption.Delete -> {
                    homeViewModel.onEvent(HomeEvent.DeleteBoard(board))
                }
            }
        }
    )
}

@Composable
private fun HomeScreen(
    gridLayout: Boolean = true,
    labels: List<SimpleLabel> = emptyList(),
    labelFilters: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {},
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    selectedSortingOption: SortingOption.Common = SortingOption.Common.NameAZ,
    onSortOptionClick: (option: SortingOption.Common) -> Unit = {},
    onBoardClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean = { false },
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<ActiveBoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit = {},
    onMenuItemClick: (
        board: BoardWithLabelsAndTasks,
        option: ActiveBoardCardMenuOption,
    ) -> Unit = { _, _ ->}
) {
    val sortingOptions = listOf(
        SortingOption.Common.Newest,
        SortingOption.Common.Oldest,
        SortingOption.Common.NameAZ,
        SortingOption.Common.NameZA,
    )
    BoardLayout(
        gridLayout = gridLayout,
        labels = labels,
        labelFilters = labelFilters,
        onLabelClick = onLabelClick,
        onRemoveFiltersClick = onRemoveFiltersClick,
        boards = boards,
        sortingOptions = sortingOptions,
        selectedSortingOption = selectedSortingOption,
        onSortOptionClick = onSortOptionClick,
        onBoardClick = onBoardClick,
        showCardMenu = showCardMenu,
        onDismissMenu = onDismissMenu,
        options = options,
        onCardOptionsClick = onCardOptionsClick,
        onMenuItemClick = { boardId, option ->
            onMenuItemClick(boardId, option as ActiveBoardCardMenuOption)
        },
    )
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