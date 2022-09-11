package com.example.finito.core.presentation.components.bars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.domain.util.menu.MenuOption
import com.example.finito.core.presentation.MENU_MIN_WIDTH

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopBarWithMenu(
    title: String,
    onMenuClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    options: List<MenuOption> = emptyList(),
    onOptionClick: (MenuOption) -> Unit = {},
) {
    TopAppBar(title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = stringResource(id = R.string.open_menu)
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = onMoreOptionsClick) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.widthIn(min = MENU_MIN_WIDTH)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(id = option.label)) },
                            onClick = { onOptionClick(option) }
                        )
                    }
                }
            }
        }, scrollBehavior = scrollBehavior
    )
}