package com.example.finito.features.boards.presentation.screen.board.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.util.menu.ActiveBoardScreenOption
import com.example.finito.core.presentation.util.menu.ArchivedBoardScreenMenuOption
import com.example.finito.core.presentation.util.menu.BoardScreenMenuOption
import com.example.finito.core.presentation.util.menu.DeletedBoardScreenMenuOption
import com.example.finito.core.presentation.components.bars.MediumTopBarWithMenu
import com.example.finito.features.boards.domain.entity.BoardState

private val activeBoardOptions = listOf(
    ActiveBoardScreenOption.EditBoard,
    ActiveBoardScreenOption.ArchiveBoard,
    ActiveBoardScreenOption.DeleteBoard,
    ActiveBoardScreenOption.DeleteCompletedTasks,
)

private val archivedBoardOptions = listOf(
    ArchivedBoardScreenMenuOption.EditBoard,
    ArchivedBoardScreenMenuOption.UnarchiveBoard,
    ArchivedBoardScreenMenuOption.DeleteBoard,
    ArchivedBoardScreenMenuOption.DeleteCompletedTasks,
)

private val deletedBoardOptions = listOf(
    DeletedBoardScreenMenuOption.EditBoard,
    DeletedBoardScreenMenuOption.RestoreBoard,
    DeletedBoardScreenMenuOption.DeleteCompletedTasks,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardTopBar(
    boardName: String,
    boardState: BoardState,
    onNavigationClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    onOptionClick: (BoardScreenMenuOption) -> Unit = {},
    disabledOptions: List<BoardScreenMenuOption> = emptyList(),

) {
   MediumTopBarWithMenu(
       title = boardName,
       onNavigationIconClick = onNavigationClick,
       navigationIcon = if (boardState == BoardState.ACTIVE)
           Icons.Outlined.Menu
       else
           Icons.Outlined.ArrowBack,
       navigationIconDescription = if (boardState == BoardState.ACTIVE)
           R.string.open_menu
       else
           R.string.go_back,
       onMoreOptionsClick = onMoreOptionsClick,
       scrollBehavior = scrollBehavior,
       showMenu = showMenu,
       onDismissMenu = onDismissMenu,
       options = when (boardState) {
           BoardState.ARCHIVED -> archivedBoardOptions
           BoardState.DELETED -> deletedBoardOptions
           else -> activeBoardOptions
       },
       onOptionClick = onOptionClick,
       disabledOptions = disabledOptions,
   )
}