package com.example.finito.features.boards.presentation.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.R
import com.example.finito.core.domain.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.HandleBackPress
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.home.components.HomeTopBar
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun HomeScreen(
    navController: NavHostController,
    drawerState: DrawerState,
    homeViewModel: HomeViewModel = hiltViewModel(),
    finishActivity: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val homeTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val isKeyboardVisible = WindowInsets.isImeVisible

    val snackbarHostState = remember { SnackbarHostState() }
    val archivedMessage = stringResource(id = R.string.board_archived)
    val deletedMessage = stringResource(id = R.string.board_moved_to_trash)
    val snackbarAction = stringResource(id = R.string.undo)

    HandleBackPress(drawerState) {
        if (homeViewModel.showSearchBar) {
            homeViewModel.onEvent(HomeEvent.ShowSearchBar(show = false))
        } else {
            finishActivity()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedContent(
                targetState = homeViewModel.showSearchBar,
                transitionSpec = {
                    (slideIntoContainer(
                        towards = AnimatedContentScope.SlideDirection.Start
                    ) with slideOutOfContainer(
                        towards = AnimatedContentScope.SlideDirection.End)
                    ).using(SizeTransform(clip = false))
                }
            ) { showSearchBar ->
                if (showSearchBar) {
                    SearchTopBar(
                        query = homeViewModel.searchQuery,
                        onQueryChange = {
                            homeViewModel.onEvent(HomeEvent.SearchBoards(it))
                        },
                        onBackClick = {
                            homeViewModel.onEvent(HomeEvent.ShowSearchBar(show = false))
                        },
                        scrollBehavior = searchTopBarScrollBehavior,
                        focusRequester = focusRequester
                    )
                } else {
                    HomeTopBar(
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        scrollBehavior = homeTopBarScrollBehavior
                    )
                }
            }
        },
        bottomBar = bottomBar@{
            if (isKeyboardVisible) return@bottomBar
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
                    navController.navigate(route = Screen.CreateBoard.route)
                }
            )
        },
        modifier = Modifier
            .nestedScroll(
                if (homeViewModel.showSearchBar)
                    searchTopBarScrollBehavior.nestedScrollConnection
                else
                    homeTopBarScrollBehavior.nestedScrollConnection
        ).noRippleClickable { focusManager.clearFocus() },
    ) { innerPadding ->
        HomeScreen(
            paddingValues = innerPadding,
            gridLayout = homeViewModel.gridLayout,
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
                        // Dismiss current Snackbar to avoid having multiple instances
                        snackbarHostState.currentSnackbarData?.dismiss()
                        scope.launch { 
                            val result = snackbarHostState.showSnackbar(
                                message = archivedMessage,
                                actionLabel = snackbarAction,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                homeViewModel.onEvent(HomeEvent.RestoreBoard)
                            }
                        }
                    }
                    ActiveBoardCardMenuOption.Delete -> {
                        homeViewModel.onEvent(HomeEvent.DeleteBoard(board))
                        // Dismiss current Snackbar to avoid having multiple instances
                        snackbarHostState.currentSnackbarData?.dismiss()
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = deletedMessage,
                                actionLabel = snackbarAction,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                homeViewModel.onEvent(HomeEvent.RestoreBoard)
                            }
                        }
                    }
                }
            }
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

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
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