package com.example.finito.features.boards.presentation.board.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import com.example.finito.core.domain.util.BoardScreenMenuOption
import com.example.finito.core.domain.util.TrashScreenMenuOption
import com.example.finito.core.presentation.components.bars.MediumTopBarWithMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardTopBar(
    boardName: String,
    onMenuClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    onOptionClick: (BoardScreenMenuOption) -> Unit = {}
) {
   MediumTopBarWithMenu(
       title = boardName,
       onMenuClick = onMenuClick,
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