package com.example.finito.core.presentation.components.bars

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.util.menu.MenuOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <M: MenuOption> SmallTopBarWithMenu(
    title: String,
    onMenuClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    options: List<M> = emptyList(),
    disabledOptions: List<M> = emptyList(),
    onOptionClick: (M) -> Unit = {},
) {
    TopAppBar(title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = stringResource(id = R.string.open_menu)
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = onMoreOptionsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.menu_vertical),
                        contentDescription = stringResource(id = R.string.more_options)
                    )
                }
                FinitoMenu(
                    show = showMenu,
                    onDismiss = onDismissMenu,
                    options = options,
                    disabledOptions = disabledOptions,
                    onOptionClick = onOptionClick
                )
            }
        }, scrollBehavior = scrollBehavior
    )
}