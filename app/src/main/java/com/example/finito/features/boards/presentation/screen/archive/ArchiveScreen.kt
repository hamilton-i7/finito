package com.example.finito.features.boards.presentation.screen.archive

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.domain.util.commonSortingOptions
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.components.bars.TopBar
import com.example.finito.core.presentation.util.menu.ArchivedBoardCardMenuOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun ArchiveScreen(
    drawerState: DrawerState,
    onShowSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
    archiveViewModel: ArchiveViewModel = hiltViewModel(),
    finishActivity: () -> Unit = {},
    onNavigateToBoardFlow: (boardId: Int) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val simpleTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val searchTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        if (archiveViewModel.showSearchBar) {
            archiveViewModel.onEvent(ArchiveEvent.ShowSearchBar(show = false))
            return@BackHandler
        }
        finishActivity()
    }

    LaunchedEffect(Unit) {
        archiveViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ArchiveViewModel.Event.ShowSnackbar -> {
                    onShowSnackbar(event.message, R.string.undo) {
                        archiveViewModel.onEvent(ArchiveEvent.RestoreBoard)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = archiveViewModel.showSearchBar,
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
                        queryState = archiveViewModel.searchQueryState.copy(
                            onValueChange = {
                                archiveViewModel.onEvent(ArchiveEvent.SearchBoards(it))
                            }
                        ),
                        onBackClick = {
                            archiveViewModel.onEvent(ArchiveEvent.ShowSearchBar(show = false))
                        },
                        scrollBehavior = searchTopBarScrollBehavior,
                    )
                } else {
                    TopBar(
                        onNavigationIconClick = {
                            scope.launch { drawerState.open() }
                        },
                        title = R.string.archive,
                        scrollBehavior = simpleTopBarScrollBehavior
                    )
                }
            }
        },
        bottomBar = {
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
        },
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
            onBoardClick = onNavigateToBoardFlow,
            selectedSortingOption = archiveViewModel.boardsOrder,
            onSortOptionClick = {
                archiveViewModel.onEvent(ArchiveEvent.SortBoards(it))
            },
            onCardOptionsClick = {
                archiveViewModel.onEvent(ArchiveEvent.ShowCardMenu(boardId = it, show = true))
            },
            showCardMenu = { archiveViewModel.selectedBoardId == it },
            onDismissMenu = {
                archiveViewModel.onEvent(ArchiveEvent.ShowCardMenu(show = false))
            },
            options = listOf(
                ArchivedBoardCardMenuOption.Unarchive,
                ArchivedBoardCardMenuOption.Delete,
            ),
            onMenuItemClick = { board, option ->
                archiveViewModel.onEvent(ArchiveEvent.ShowCardMenu(show = false))
                when (option) {
                    ArchivedBoardCardMenuOption.Unarchive -> {
                        archiveViewModel.onEvent(ArchiveEvent.UnarchiveBoard(board))
                    }
                    ArchivedBoardCardMenuOption.Delete -> {
                        archiveViewModel.onEvent(ArchiveEvent.MoveBoardToTrash(board))
                    }
                }
            }
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
    onBoardClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean = { false },
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<ArchivedBoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit = {},
    onMenuItemClick: (
        board: BoardWithLabelsAndTasks,
        option: ArchivedBoardCardMenuOption,
    ) -> Unit = { _, _ ->}
) {
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
            selectedSortingOption = selectedSortingOption,
            sortingOptions = commonSortingOptions,
            onSortOptionClick = onSortOptionClick,
            onBoardClick = onBoardClick,
            showCardMenu = showCardMenu,
            onDismissMenu = onDismissMenu,
            options = options,
            onCardOptionsClick = onCardOptionsClick,
            onMenuItemClick = { boardId, option ->
                onMenuItemClick(boardId, option as ArchivedBoardCardMenuOption)
            }
        )
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