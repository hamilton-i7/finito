package com.example.finito.features.boards.presentation.trash

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.core.domain.util.menu.DeletedBoardCardMenuOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.DialogType
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.boards.presentation.trash.components.TrashDialogs
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    navController: NavHostController,
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    trashViewModel: TrashViewModel = hiltViewModel(),
    finishActivity: () -> Unit = {},
    showSnackbar: (message: Int, () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()

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
                is TrashViewModel.Event.ShowSnackbar -> {
                    showSnackbar(event.message) {
                        trashViewModel.onEvent(TrashEvent.UndoRestore)
                    }
                }
            }
        }
    }

    if (appViewModel.dialogType != null) {
        TrashDialogs(trashViewModel = trashViewModel, appViewModel = appViewModel)
    }

    TrashScreen(
        gridLayout = trashViewModel.gridLayout,
        boards = trashViewModel.boards,
        onBoardClick = {
            val route = "${Screen.Board.prefix}/${it}"
            navController.navigate(route)
        },
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
                    appViewModel.onEvent(
                        AppEvent.ShowDialog(type = DialogType.DeleteBoard(board.board))
                    )
                }
            }
        }
    )
}

@Composable
private fun TrashScreen(
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
        }
    )
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