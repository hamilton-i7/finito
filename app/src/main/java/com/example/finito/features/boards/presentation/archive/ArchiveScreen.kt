package com.example.finito.features.boards.presentation.archive

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.domain.util.menu.ArchivedBoardCardMenuOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardLayout
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.ui.theme.FinitoTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    navController: NavHostController,
    appViewModel: AppViewModel,
    archiveViewModel: ArchiveViewModel = hiltViewModel(),
    showSnackbar: (message: Int, () -> Unit) -> Unit,
    drawerState: DrawerState,
    finishActivity: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            return@BackHandler
        }
        if (appViewModel.showSearchbar) {
            appViewModel.onEvent(
                screen = Screen.Archive,
                event = AppEvent.Archive.ShowSearchBar(show = false)
            )
            return@BackHandler
        }
        finishActivity()
    }

    LaunchedEffect(Unit) {
        archiveViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ArchiveViewModel.Event.ShowSnackbar -> {
                    showSnackbar(event.message) {
                        archiveViewModel.onEvent(ArchiveEvent.RestoreBoard)
                    }
                }
            }
        }
    }

    SideEffect {
        scope.launch {
            appViewModel.eventFlow.collectLatest {
                when (val event = it as AppViewModel.Event.Archive) {
                    is AppViewModel.Event.Archive.SearchBoards -> {
                        archiveViewModel.onEvent(ArchiveEvent.SearchBoards(event.query))
                    }
                }
            }
        }
    }

    ArchiveScreen(
        gridLayout = appViewModel.gridLayout,
        labels = archiveViewModel.labels,
        labelFilters = archiveViewModel.labelFilters,
        onLabelClick = {
            archiveViewModel.onEvent(ArchiveEvent.AddFilter(it))
        },
        onRemoveFiltersClick = {
            archiveViewModel.onEvent(ArchiveEvent.RemoveFilters)
        },
        boards = archiveViewModel.boards,
        onBoardClick = {
            val route = "${Screen.Board.prefix}/${it}"
            navController.navigate(route)
        },
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
                    archiveViewModel.onEvent(ArchiveEvent.DeleteBoard(board))
                }
            }
        }
    )
}

@Composable
private fun ArchiveScreen(
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
        selectedSortingOption = selectedSortingOption,
        sortingOptions = sortingOptions,
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