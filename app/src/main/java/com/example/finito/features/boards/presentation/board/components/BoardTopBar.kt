package com.example.finito.features.boards.presentation.board.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.domain.util.menu.*
import com.example.finito.core.presentation.components.bars.MediumTopBarWithMenu
import com.example.finito.features.boards.domain.BoardState

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
    onOptionClick: (BoardScreenMenuOption) -> Unit = {}
) {
   MediumTopBarWithMenu(
       title = boardName,
       onNavigationIconClick = onNavigationClick,
       navigationIcon = if (boardState == BoardState.Active) Icons.Outlined.Menu else Icons.Outlined.ArrowBack,
       navigationIconDescription = if (boardState == BoardState.Active) R.string.open_menu else R.string.go_back,
       onMoreOptionsClick = onMoreOptionsClick,
       scrollBehavior = scrollBehavior,
       showMenu = showMenu,
       onDismissMenu = onDismissMenu,
       options = when (boardState) {
           BoardState.Active -> activeBoardOptions
           BoardState.Archived -> archivedBoardOptions
           BoardState.Deleted -> deletedBoardOptions
       },
       onOptionClick = { onOptionClick(it as BoardScreenMenuOption) }
   )
}