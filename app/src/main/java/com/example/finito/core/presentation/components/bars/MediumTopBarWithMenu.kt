package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.finito.R
import com.example.finito.core.domain.util.TopBarMenuOption
import com.example.finito.core.presentation.MENU_MIN_WIDTH

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumTopBarWithMenu(
    title: String,
    onNavigationIconClick: () -> Unit = {},
    navigationIcon: ImageVector = Icons.Outlined.Menu,
    @StringRes navigationIconDescription: Int = R.string.open_menu,
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    options: List<TopBarMenuOption> = emptyList(),
    onOptionClick: (TopBarMenuOption) -> Unit = {},
) {
    MediumTopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = stringResource(id = navigationIconDescription)
                )
            }
        },
        title = { Text(title, overflow = TextOverflow.Ellipsis, maxLines = 1) },
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
        },
        scrollBehavior = scrollBehavior
    )
}