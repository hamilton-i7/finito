package com.example.finito.features.boards.presentation.screen.archive

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.components.EmptyContent
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.TopBar
import com.example.finito.core.presentation.util.menu.ArchivedBoardCardMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.components.SortBoardsSheetContent
import com.example.finito.features.boards.presentation.screen.archive.components.ArchiveDialogs
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun ArchiveScreen(
    appViewModel: AppViewModel,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onShowSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
    archiveViewModel: ArchiveViewModel = hiltViewModel(),
    finishActivity: () -> Unit = {},
    onNavigateToBoardFlow: (boardId: Int) -> Unit = {},
    onNavigateToSearchBoards: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val archiveTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        finishActivity()
    }

    LaunchedEffect(Unit) {
        archiveViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ArchiveViewModel.Event.ShowError -> {
                    archiveViewModel.onEvent(ArchiveEvent.ShowDialog(
                        type = ArchiveEvent.DialogType.Error(message = event.error)
                    ))
                }
                is ArchiveViewModel.Event.Snackbar.BoardStateChanged -> {
                    onShowSnackbar(event.message, event.actionLabel) {
                        appViewModel.onEvent(AppEvent.UndoBoardChange(board = event.board))
                    }
                }
                ArchiveViewModel.Event.NavigateToSearchBoards -> onNavigateToSearchBoards()
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetBackgroundColor = finitoColors.surface,
        sheetContent = {
            SortBoardsSheetContent(
                selectedOption = archiveViewModel.boardsOrder,
                onClick = {
                    archiveViewModel.onEvent(ArchiveEvent.SortBoards(it))
                    scope.launch { bottomSheetState.hide() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    onNavigationIconClick = {
                        scope.launch { drawerState.open() }
                    },
                    title = R.string.archive,
                    scrollBehavior = archiveTopBarScrollBehavior
                )
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
                        archiveViewModel.onEvent(ArchiveEvent.EnableSearch)
                    }
                )
            },
            modifier = Modifier.nestedScroll(archiveTopBarScrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            if (archiveViewModel.loading) return@Scaffold

            ArchiveDialogs(archiveViewModel)
            ArchiveScreen(
                paddingValues = innerPadding,
                gridLayout = archiveViewModel.gridLayout,
                boards = archiveViewModel.boards,
                onBoardClick = onNavigateToBoardFlow,
                selectedSortingOption = archiveViewModel.boardsOrder,
                onSortIndicatorClick = {
                    scope.launch { bottomSheetState.show() }
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
}

@Composable
private fun ArchiveScreen(
    paddingValues: PaddingValues = PaddingValues(),
    gridLayout: Boolean = true,
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    selectedSortingOption: SortingOption.Common = SortingOption.Common.Default,
    onSortIndicatorClick: () -> Unit = {},
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
        Crossfade(targetState = boards.isEmpty()) { isEmpty ->
            when (isEmpty) {
                true -> {
                    EmptyContent(
                        icon = R.drawable.open_box,
                        title = R.string.no_boards_archived_title,
                        contentText = R.string.no_boards_archived_content
                    )
                }
                false -> {
                    BoardLayout(
                        gridLayout = gridLayout,
                        boards = boards,
                        selectedSortingOption = selectedSortingOption,
                        onSortOptionClick = onSortIndicatorClick,
                        onBoardClick = onBoardClick,
                        showCardMenu = showCardMenu,
                        onDismissMenu = onDismissMenu,
                        options = options,
                        onCardOptionsClick = onCardOptionsClick,
                        onMenuItemClick = { boardId, option ->
                            onMenuItemClick(boardId, option as ArchivedBoardCardMenuOption)
                        },
                    )
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun ArchiveScreenPreview() {
    FinitoTheme {
        Surface {
            ArchiveScreen(
                boards = BoardWithLabelsAndTasks.dummyBoards,
                selectedSortingOption = SortingOption.Common.Newest
            )
        }
    }
}