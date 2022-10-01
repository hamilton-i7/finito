package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.finito.R
import com.example.finito.core.presentation.util.menu.MenuOption
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.util.TestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <M: MenuOption> MediumTopBarWithMenu(
    title: String,
    onNavigationIconClick: () -> Unit = {},
    navigationIcon: ImageVector = Icons.Outlined.Menu,
    @StringRes navigationIconDescription: Int = R.string.open_menu,
    onMoreOptionsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showMenu: Boolean = false,
    onDismissMenu: () -> Unit = {},
    options: List<M> = emptyList(),
    onOptionClick: (M) -> Unit = {},
    disabledOptions: List<M> = emptyList(),
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
                IconButton(
                    onClick = onMoreOptionsClick,
                    modifier = Modifier.testTag(TestTags.MENU_BUTTON)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options)
                    )
                }
                FinitoMenu(
                    show = showMenu,
                    onDismiss = onDismissMenu,
                    options = options,
                    onOptionClick = onOptionClick,
                    disabledOptions = disabledOptions,
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}