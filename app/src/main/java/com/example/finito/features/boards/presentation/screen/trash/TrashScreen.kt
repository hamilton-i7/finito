package com.example.finito.features.boards.presentation.screen.trash

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.components.EmptyContent
import com.example.finito.core.presentation.util.menu.DeletedBoardCardMenuOption
import com.example.finito.core.presentation.util.menu.TrashScreenMenuOption
import com.example.finito.core.presentation.util.preview.CompletePreviews
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.screen.trash.components.TrashDialogs
import com.example.finito.features.boards.presentation.screen.trash.components.TrashTopBar
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    appViewModel: AppViewModel,
    trashViewModel: TrashViewModel = hiltViewModel(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onShowSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
    finishActivity: () -> Unit = {},
    onNavigateToBoardFlow: (boardId: Int) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        finishActivity()
    }

    LaunchedEffect(Unit) {
        trashViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is TrashViewModel.Event.ShowError -> {
                    trashViewModel.onEvent(TrashEvent.ShowDialog(
                        type = TrashEvent.DialogType.Error(message = event.error)
                    ))
                }
                is TrashViewModel.Event.Snackbar.BoardStateChanged -> {
                    onShowSnackbar(event.message, event.actionLabel) {
                        appViewModel.onEvent(AppEvent.UndoBoardChange(board = event.board))
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TrashTopBar(
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
                onMoreOptionsClick = {
                    trashViewModel.onEvent(TrashEvent.ShowMenu(show = true))
                },
                scrollBehavior = topBarScrollBehavior,
                showMenu = trashViewModel.showMenu,
                onDismissMenu = {
                    trashViewModel.onEvent(TrashEvent.ShowMenu(show = false))
                },
                onOptionClick = {
                    trashViewModel.onEvent(TrashEvent.ShowMenu(show = false))
                    when (it) {
                        TrashScreenMenuOption.EmptyTrash -> trashViewModel.onEvent(
                            TrashEvent.ShowDialog(
                                type = TrashEvent.DialogType.EmptyTrash
                            )
                        )
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (trashViewModel.loading) return@Scaffold

        TrashDialogs(trashViewModel)
        TrashScreen(
            paddingValues = innerPadding,
            gridLayout = trashViewModel.gridLayout,
            boards = trashViewModel.boards,
            onBoardClick = onNavigateToBoardFlow,
            onCardOptionsClick = {
                trashViewModel.onEvent(TrashEvent.ShowCardMenu(boardId = it, show = true))
            },
            showCardMenu = { trashViewModel.selectedBoardId == it },
            onDismissMenu = {
                trashViewModel.onEvent(TrashEvent.ShowCardMenu(show = false))
            },
            options = listOf(
                DeletedBoardCardMenuOption.Restore,
                DeletedBoardCardMenuOption.DeleteForever,
            ),
            onMenuItemClick = { board, option ->
                trashViewModel.onEvent(TrashEvent.ShowCardMenu(show = false))
                when (option) {
                    DeletedBoardCardMenuOption.Restore -> {
                        trashViewModel.onEvent(TrashEvent.RestoreBoard(board))
                    }
                    DeletedBoardCardMenuOption.DeleteForever -> {
                        trashViewModel.onEvent(TrashEvent.ShowDialog(
                            type = TrashEvent.DialogType.DeleteBoard(board.board)
                        ))
                    }
                }
            }
        )
    }
}

@Composable
private fun TrashScreen(
    paddingValues: PaddingValues = PaddingValues(),
    gridLayout: Boolean = true,
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    onBoardClick: (boardId: Int) -> Unit = {},
    showCardMenu: (boardId: Int) -> Boolean = { false },
    onDismissMenu: (boardId: Int) -> Unit = {},
    options: List<DeletedBoardCardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit = {},
    onMenuItemClick: (
        board: BoardWithLabelsAndTasks,
        option: DeletedBoardCardMenuOption,
    ) -> Unit = { _, _ ->}
) {
    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        Crossfade(targetState = boards.isEmpty()) {
            when (it) {
                true -> {
                    EmptyContent(
                        icon = R.drawable.full_trash,
                        title = R.string.no_boards_deleted_title,
                        contentText = R.string.no_boards_deleted_content
                    )
                }
                false -> {
                    BoardLayout(
                        gridLayout = gridLayout,
                        boards = boards,
                        onBoardClick = onBoardClick,
                        showCardMenu = showCardMenu,
                        onDismissMenu = onDismissMenu,
                        options = options,
                        onCardOptionsClick = onCardOptionsClick,
                        onMenuItemClick = { boardId, option ->
                            onMenuItemClick(boardId, option as DeletedBoardCardMenuOption)
                        },
                    )
                }
            }
        }
    }
}

@CompletePreviews
@Composable
private fun TrashScreenPreview() {
    FinitoTheme {
        Surface {
            TrashScreen(boards = BoardWithLabelsAndTasks.dummyBoards)
        }
    }
}