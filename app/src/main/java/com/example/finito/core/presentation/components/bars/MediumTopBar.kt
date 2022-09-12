package com.example.finito.core.presentation.components.bars

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TopBarState
import com.example.finito.features.boards.presentation.board.utils.BoardScreenDefaults
import com.example.finito.features.boards.presentation.trash.utils.TrashScreenDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumTopBar(
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    navController: NavController,
    currentRoute: String?,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val topBarState: TopBarState?

    when (currentRoute) {
        Screen.Board.route -> {
            topBarState = BoardScreenDefaults.rememberTopBarState(
                appViewModel = appViewModel,
                drawerState = drawerState,
                navController = navController
            )
        }
        Screen.Trash.route -> {
            topBarState = TrashScreenDefaults.rememberTopBarState(
                appViewModel = appViewModel,
                drawerState = drawerState,
            )
        }
        else -> {
            topBarState = null
        }
    }
    if (topBarState == null) return

    MediumTopAppBar(
        navigationIcon = {
            IconButton(onClick = topBarState.onNavigationIconClick) {
                Icon(
                    imageVector = topBarState.navigationIcon,
                    contentDescription = stringResource(id = topBarState.navigationIconDescription)
                )
            }
        },
        title = {
            Text(
                text = topBarState.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        actions = topBarState.actions,
        scrollBehavior = scrollBehavior
    )
}