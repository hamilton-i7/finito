package com.example.finito.features.boards.presentation.board.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.domain.util.BoardScreenMenuOption
import com.example.finito.core.presentation.components.bars.MediumTopBarWithMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardTopBar(
    boardName: String,
    activeBoard: Boolean = true,
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
       navigationIcon = if (activeBoard) Icons.Outlined.Menu else Icons.Outlined.ArrowBack,
       navigationIconDescription = if (activeBoard) R.string.open_menu else R.string.go_back,
       onMoreOptionsClick = onMoreOptionsClick,
       scrollBehavior = scrollBehavior,
       showMenu = showMenu,
       onDismissMenu = onDismissMenu,
       options = listOf(
           BoardScreenMenuOption.EditBoard,
           BoardScreenMenuOption.ArchiveBoard,
           BoardScreenMenuOption.DeleteBoard,
           BoardScreenMenuOption.DeleteCompletedTasks,
       ),
       onOptionClick = { onOptionClick(it as BoardScreenMenuOption) }
   )
}