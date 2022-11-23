package com.example.finito.features.boards.presentation.screen.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.components.EmptyContent
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.core.presentation.util.menu.ActiveBoardCardMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.components.SortBoardsSheetContent
import com.example.finito.features.boards.presentation.screen.home.components.HomeDialogs
import com.example.finito.features.boards.presentation.screen.home.components.HomeTopBar
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableLazyGridState
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    appViewModel: AppViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onNavigateToCreateBoard: () -> Unit = {},
    onNavigateToBoard: (boardId: Int) -> Unit = {},
    onNavigateToSearchBoards: () -> Unit = {},
    finishActivity: () -> Unit = {},
    onShowSnackbar: (
        message: Int,
        actionLabel: Int?,
        onActionClick: () -> Unit
    ) -> Unit = {_, _, _ -> },
) {
    val scope = rememberCoroutineScope()
    val homeTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

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
        canDragOver = { draggedOver, _ -> homeViewModel.canDrag(draggedOver) },
        onDragEnd = { from, to ->
            homeViewModel.onEvent(HomeEvent.SaveTasksOrder(from, to))
        }
    )
    val reorderableGridState = rememberReorderableLazyGridState(
        onMove = { from, to ->
            homeViewModel.onEvent(HomeEvent.ReorderTasks(from, to))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        canDragOver = { draggedOver, _ -> homeViewModel.canDrag(draggedOver) },
        onDragEnd = { from, to ->
            homeViewModel.onEvent(HomeEvent.SaveTasksOrder(from, to))
        }
    )

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        finishActivity()
    }

    LaunchedEffect(Unit) {
        homeViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is HomeViewModel.Event.ShowError -> {
                    homeViewModel.onEvent(HomeEvent.ShowDialog(
                        type = HomeEvent.DialogType.Error(message = event.error)
                    ))
                }
                is HomeViewModel.Event.Snackbar.BoardStateChanged -> {
                    onShowSnackbar(event.message, event.actionLabel) {
                        appViewModel.onEvent(AppEvent.UndoBoardChange(board = event.board))
                    }
                }
                HomeViewModel.Event.NavigateToSearchBoards -> onNavigateToSearchBoards()
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetBackgroundColor = finitoColors.surface,
        sheetContent = {
            SortBoardsSheetContent(
                selectedOption = homeViewModel.boardsOrder,
                onClick = {
                    homeViewModel.onEvent(HomeEvent.SortBoards(it))
                    scope.launch { bottomSheetState.hide() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    scrollBehavior = homeTopBarScrollBehavior
                )
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
                        homeViewModel.onEvent(HomeEvent.EnableSearch)
                    },
                    onFabClick = onNavigateToCreateBoard
                )
            },
            modifier = Modifier
                .nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection)
                .testTag(TestTags.HOME_SCREEN),
        ) { innerPadding ->
            if (homeViewModel.loading) return@Scaffold

            HomeDialogs(homeViewModel)
            HomeScreen(
                paddingValues = innerPadding,
                reorderableListState = reorderableListState,
                reorderableGridState = reorderableGridState,
                gridLayout = homeViewModel.gridLayout,
                boards = homeViewModel.boards,
                onBoardClick = onNavigateToBoard,
                selectedSortingOption = homeViewModel.boardsOrder,
                onSortIndicatorClick = {
                    scope.launch { bottomSheetState.show() }
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
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    selectedSortingOption: SortingOption.Common = SortingOption.Common.Default,
    onSortIndicatorClick: () -> Unit = {},
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
    val allowDrag = selectedSortingOption == SortingOption.Common.Custom

    LaunchedEffect(reorderableListState.draggingItemKey) {
        if (reorderableListState.draggingItemKey == null
            || selectedSortingOption != SortingOption.Common.Custom) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    LaunchedEffect(reorderableGridState.draggingItemKey) {
        if (reorderableGridState.draggingItemKey == null
            || selectedSortingOption != SortingOption.Common.Custom) return@LaunchedEffect
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        Crossfade(targetState = boards.isEmpty()) { isEmpty ->
            when (isEmpty) {
                true -> {
                    EmptyContent(
                        icon = R.drawable.todo_list,
                        title = R.string.no_boards_created_title,
                        contentText = R.string.no_boards_created_content,
                    )
                }
                false -> {
                    BoardLayout(
                        gridLayout = gridLayout,
                        allowDrag = allowDrag,
                        reorderableGridState = reorderableGridState,
                        reorderableListState = reorderableListState,
                        boards = boards,
                        selectedSortingOption = selectedSortingOption,
                        onSortOptionClick = onSortIndicatorClick,
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
        }
    }
}
@CompletePreviews
@Composable
private fun HomeScreenPreview() {
    FinitoTheme {
        Surface {
            HomeScreen(
                boards = BoardWithLabelsAndTasks.dummyBoards,
                selectedSortingOption = SortingOption.Common.Newest
            )
        }
    }
}