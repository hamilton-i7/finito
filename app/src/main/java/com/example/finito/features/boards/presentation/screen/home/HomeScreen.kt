package com.example.finito.features.boards.presentation.screen.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.domain.util.commonSortingOptions
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.core.presentation.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.screen.home.components.HomeTopBar
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableLazyGridState
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onNavigateToCreateBoard: () -> Unit = {},
    onNavigateToBoard: (boardId: Int) -> Unit = {},
    finishActivity: () -> Unit = {},
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit
    ) -> Unit = {_, _, _ -> },
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val homeTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    val reorderableListState = rememberReorderableLazyListState(
        listState = listState,
        onMove = { from, to ->
            // Make sure the target item is visible before switching places
            if (listState.firstVisibleItemIndex == to.index) {
                scope.launch { listState.scrollToItem(to.index, scrollOffset = -1) }
            }
            homeViewModel.onEvent(HomeEvent.ReorderTasks(from, to))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        canDragOver = homeViewModel::canDrag,
        onDragEnd = { from, to ->
            homeViewModel.onEvent(HomeEvent.SaveTasksOrder(from, to))
        }
    )
    val reorderableGridState = rememberReorderableLazyGridState(
        onMove = { from, to ->
            homeViewModel.onEvent(HomeEvent.ReorderTasks(from, to))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        canDragOver = homeViewModel::canDrag,
        onDragEnd = { from, to ->
            homeViewModel.onEvent(HomeEvent.SaveTasksOrder(from, to))
        }
    )

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        if (homeViewModel.showSearchBar) {
            homeViewModel.onEvent(HomeEvent.ShowSearchBar(show = false))
            return@BackHandler
        }
        finishActivity()
    }

    LaunchedEffect(Unit) {
        homeViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is HomeViewModel.Event.ShowSnackbar -> {
                    onShowSnackbar(event.message, R.string.undo) {
                        homeViewModel.onEvent(HomeEvent.RestoreBoard)
                    }
                }
            }
        }
    }

    Scaffold(
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
                        queryState = homeViewModel.searchQueryState.copy(
                            onValueChange = {
                                homeViewModel.onEvent(HomeEvent.SearchBoards(it))
                            }
                        ),
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
        bottomBar = {
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
                onFabClick = onNavigateToCreateBoard
            )
        },
        modifier = Modifier
            .nestedScroll(
                if (homeViewModel.showSearchBar)
                    searchTopBarScrollBehavior.nestedScrollConnection
                else
                    homeTopBarScrollBehavior.nestedScrollConnection
            )
            .noRippleClickable { focusManager.clearFocus() }
            .testTag(TestTags.HOME_SCREEN),
    ) { innerPadding ->
        HomeScreen(
            paddingValues = innerPadding,
            reorderableListState = reorderableListState,
            reorderableGridState = reorderableGridState,
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
            onBoardClick = onNavigateToBoard,
            selectedSortingOption = homeViewModel.boardsOrder,
            onSortOptionClick = onSortOptionClick@{
                if (homeViewModel.boardsOrder == it){
                    homeViewModel.onEvent(HomeEvent.SortBoards(sortingOption = null))
                    return@onSortOptionClick
                }
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
                        homeViewModel.onEvent(HomeEvent.MoveBoardToTrash(board))
                    }
                }
            }
        )
    }
}

@Composable
private fun HomeScreen(
    paddingValues: PaddingValues = PaddingValues(),
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
    reorderableListState: ReorderableLazyListState = rememberReorderableLazyListState(
        onMove = { _, _ -> }
    ),
    reorderableGridState: ReorderableLazyGridState = rememberReorderableLazyGridState(
        onMove = { _, _ -> }
    ),
    gridLayout: Boolean = true,
    labels: List<SimpleLabel> = emptyList(),
    labelFilters: List<Int> = emptyList(),
    onLabelClick: (labelId: Int) -> Unit = {},
    onRemoveFiltersClick: () -> Unit = {},
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    selectedSortingOption: SortingOption.Common? = null,
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
    LaunchedEffect(reorderableListState.draggingItemKey) {
        if (reorderableListState.draggingItemKey == null) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    LaunchedEffect(reorderableGridState.draggingItemKey) {
        if (reorderableGridState.draggingItemKey == null) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        BoardLayout(
            gridLayout = gridLayout,
            reorderableGridState = reorderableGridState,
            reorderableListState = reorderableListState,
            labels = labels,
            labelFilters = labelFilters,
            onLabelClick = onLabelClick,
            onRemoveFiltersClick = onRemoveFiltersClick,
            boards = boards,
            sortingOptions = commonSortingOptions,
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

@CompletePreviews
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