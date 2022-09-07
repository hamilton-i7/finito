package com.example.finito.features.boards.presentation.trash

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.core.domain.util.TrashScreenMenuOption
import com.example.finito.core.presentation.components.bars.TrashTopBar
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.presentation.components.BoardsGrid
import com.example.finito.features.boards.presentation.components.BoardsList
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
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                            TrashEvent.EmptyTrash
                        )
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        TrashScreen(
            paddingValues = innerPadding,
            gridLayout = trashViewModel.gridLayout,
            boards = trashViewModel.boards,
        )
    }
}

@Composable
private fun TrashScreen(
    paddingValues: PaddingValues = PaddingValues(),
    gridLayout: Boolean = true,
    boards: List<BoardWithLabelsAndTasks> = emptyList(),
    onBoardClick: (boardId: Int) -> Unit = {}
) {
    val contentPadding = PaddingValues(
        vertical = 12.dp,
        horizontal = 16.dp
    )
    Surface(modifier = Modifier.padding(paddingValues)) {
        if (gridLayout) {
            BoardsGrid(
                contentPadding = contentPadding,
                boards = boards,
                onBoardClick = onBoardClick
            )
        } else {
            BoardsList(
                contentPadding = contentPadding,
                boards = boards,
                onBoardClick = onBoardClick
            )
        }
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