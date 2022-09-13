package com.example.finito.features.boards.presentation.board.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.domain.util.menu.ActiveBoardScreenOption
import com.example.finito.core.domain.util.menu.ArchivedBoardScreenMenuOption
import com.example.finito.core.domain.util.menu.BoardScreenMenuOption
import com.example.finito.core.domain.util.menu.DeletedBoardScreenMenuOption
import com.example.finito.core.presentation.components.bars.MediumTopBarWithMenu
import com.example.finito.features.boards.domain.entity.Board

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
    board: Board,
    onNavigationClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    onOptionClick: (BoardScreenMenuOption) -> Unit = {}
) {
    val isInactiveBoard = board.archived || board.deleted

   MediumTopBarWithMenu(
       title = board.name,
       onNavigationIconClick = onNavigationClick,
       navigationIcon = if (isInactiveBoard) Icons.Outlined.ArrowBack else Icons.Outlined.Menu,
       navigationIconDescription = if (isInactiveBoard) R.string.go_back else R.string.open_menu,
       onMoreOptionsClick = onMoreOptionsClick,
       scrollBehavior = scrollBehavior,
       showMenu = showMenu,
       onDismissMenu = onDismissMenu,
       options = if (board.archived)
           archivedBoardOptions
       else if (board.deleted)
           deletedBoardOptions
       else
           activeBoardOptions,
       onOptionClick = onOptionClick
   )
}