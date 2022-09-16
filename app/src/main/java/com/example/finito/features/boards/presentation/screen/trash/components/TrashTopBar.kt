package com.example.finito.features.boards.presentation.screen.trash.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.menu.TrashScreenMenuOption
import com.example.finito.core.presentation.components.bars.SmallTopBarWithMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashTopBar(
    onMenuClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    onOptionClick: (TrashScreenMenuOption) -> Unit = {}
) {
    SmallTopBarWithMenu(
        title = stringResource(id = R.string.trash),
        onMenuClick = onMenuClick,
        onMoreOptionsClick = onMoreOptionsClick,
        scrollBehavior = scrollBehavior,
        showMenu = showMenu,
        onDismissMenu = onDismissMenu,
        options = listOf<TrashScreenMenuOption>(
            TrashScreenMenuOption.EmptyTrash
        ),
        onOptionClick = onOptionClick
    )
}