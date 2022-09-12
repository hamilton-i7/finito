package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TopBarState
import com.example.finito.features.boards.presentation.archive.utils.ArchiveScreenDefaults
import com.example.finito.features.boards.presentation.trash.utils.TrashScreenDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navigationIcon: ImageVector = Icons.Outlined.Menu,
    @StringRes navigationIconDescription: Int = R.string.open_menu,
    onNavigationIconClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    @StringRes title: Int,
) {
    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = stringResource(id = navigationIconDescription)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    currentRoute: String?,
    scrollBehavior: TopAppBarScrollBehavior,
    searchBarScrollBehavior: TopAppBarScrollBehavior,
) {
    val topBarState: TopBarState?
    var onQueryChange: (TextFieldValue) -> Unit = {}

    when (currentRoute) {
        Screen.Archive.route -> {
            topBarState = ArchiveScreenDefaults.rememberTopBarState(
                appViewModel = appViewModel,
                drawerState = drawerState,
            )
            onQueryChange = {
                appViewModel.onEvent(
                    screen = Screen.Archive,
                    event = AppEvent.Archive.SearchBoards(it)
                )
            }
        }
        Screen.Trash.route -> {
            topBarState = TrashScreenDefaults.rememberTopBarState(
                appViewModel = appViewModel,
                drawerState = drawerState,
            )
        }
        else -> {
            topBarState = null
            onQueryChange = {}
        }
    }
    if (topBarState == null) return

    AnimatedContent(
        targetState = appViewModel.showSearchbar,
        transitionSpec = {
            (slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Start
            ) with slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.End)
                    ).using(SizeTransform(clip = false))
        }
    ) { showingSearchBar ->
        if (showingSearchBar) {
            SearchTopBar(
                query = appViewModel.searchQuery,
                onQueryChange = onQueryChange,
                onBackClick = topBarState.onNavigationIconClick,
                scrollBehavior = searchBarScrollBehavior,
            )
        } else {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = topBarState.onNavigationIconClick) {
                        Icon(
                            imageVector = topBarState.navigationIcon,
                            contentDescription = stringResource(
                                id = topBarState.navigationIconDescription
                            )
                        )
                    }
                },
                title = { Text(text = topBarState.title) },
                actions = topBarState.actions,
                scrollBehavior = scrollBehavior,
            )
        }
    }
}