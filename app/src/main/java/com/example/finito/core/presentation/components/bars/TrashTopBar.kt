package com.example.finito.core.presentation.components.bars

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.domain.util.TrashScreenMenuOption

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
        onOptionClick = {
            onOptionClick(it as TrashScreenMenuOption)
        }
    )
}