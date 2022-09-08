package com.example.finito.features.boards.presentation.trash

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.R
import com.example.finito.core.domain.util.DeletedBoardMenuOption
import com.example.finito.core.domain.util.TrashScreenMenuOption
import com.example.finito.core.presentation.components.bars.TrashTopBar
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.trash.components.TrashDialogs
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    navHostController: NavHostController,
    drawerState: DrawerState,
    trashViewModel: TrashViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val snackbarHostState = remember { SnackbarHostState() }
    val restoredMessage = stringResource(id = R.string.board_was_restored)
    val snackbarAction = stringResource(id = R.string.undo)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                            TrashEvent.ShowDeleteDialog(
                                show = true,
                                type = TrashEvent.DialogType.EmptyTrash
                            )
                        )
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (trashViewModel.showDialog) {
            TrashDialogs(trashViewModel)
        }

        TrashScreen(
            paddingValues = innerPadding,
            gridLayout = trashViewModel.gridLayout,
            boards = trashViewModel.boards,
            onCardOptionsClick = {
                trashViewModel.onEvent(TrashEvent.ShowCardMenu(boardId = it, show = true))
            },
            showCardMenu = { trashViewModel.selectedBoardId == it },
            onDismissMenu = {
                trashViewModel.onEvent(TrashEvent.ShowCardMenu(show = false))
            },
            options = listOf(
                DeletedBoardMenuOption.Restore,
                DeletedBoardMenuOption.DeleteForever,
            ),
            onMenuItemClick = { board, option ->
                trashViewModel.onEvent(TrashEvent.ShowCardMenu(show = false))
                when (option) {
                    DeletedBoardMenuOption.Restore -> {
                        trashViewModel.onEvent(TrashEvent.RestoreBoard(board))

                        // Dismiss current Snackbar to avoid having multiple instances
                        snackbarHostState.currentSnackbarData?.dismiss()
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = restoredMessage,
                                actionLabel = snackbarAction
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                trashViewModel.onEvent(TrashEvent.UndoRestore)
                            }
                        }
                    }
                    DeletedBoardMenuOption.DeleteForever -> {
                        trashViewModel.onEvent(TrashEvent.ShowDeleteDialog(
                            show = true,
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
    options: List<DeletedBoardMenuOption> = emptyList(),
    onCardOptionsClick: (boardId: Int) -> Unit = {},
    onMenuItemClick: (
        board: BoardWithLabelsAndTasks,
        option: DeletedBoardMenuOption,
    ) -> Unit = { _, _ ->}
) {
    Surface(modifier = Modifier.padding(paddingValues)) {
        BoardLayout(
            gridLayout = gridLayout,
            boards = boards,
            onBoardClick = onBoardClick,
            showCardMenu = showCardMenu,
            onDismissMenu = onDismissMenu,
            options = options,
            onCardOptionsClick = onCardOptionsClick,
            onMenuItemClick = { boardId, option ->
                onMenuItemClick(boardId, option as DeletedBoardMenuOption)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrashScreenPreview() {
    FinitoTheme {
        Surface {
            TrashScreen(boards = BoardWithLabelsAndTasks.dummyBoards)
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun TrashScreenPreviewDark() {
    FinitoTheme {
        Surface {
            TrashScreen(boards = BoardWithLabelsAndTasks.dummyBoards)
        }
    }
}