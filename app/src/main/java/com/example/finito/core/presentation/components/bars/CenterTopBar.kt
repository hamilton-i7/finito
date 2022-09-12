package com.example.finito.core.presentation.components.bars

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.presentation.archive.utils.atArchiveScreen
import com.example.finito.features.boards.presentation.trash.utils.atTrashScreen
import com.example.finito.core.presentation.util.TopBarState
import com.example.finito.features.boards.presentation.home.components.HomeTopBar

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CenterTopBar(
    onMenuClick: () -> Unit,
    showSearchBar: Boolean,
    onSearchBarNavigationIconClick: () -> Unit,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    searchBarScrollBehavior: TopAppBarScrollBehavior,
) {
    val focusRequester = remember { FocusRequester() }

    AnimatedContent(
        targetState = showSearchBar,
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
                query = searchQuery,
                onBackClick = onSearchBarNavigationIconClick,
                onQueryChange = onSearchQueryChange,
                scrollBehavior = searchBarScrollBehavior,
                focusRequester = focusRequester
            )
        } else {
            HomeTopBar(
                onMenuClick = onMenuClick,
                scrollBehavior = scrollBehavior
            )
        }
    }
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
    val scope = rememberCoroutineScope()

    val topBarState: TopBarState = when (currentRoute) {
        Screen.Archive.route -> atArchiveScreen(scope, drawerState)
        Screen.Trash.route -> atTrashScreen(appViewModel = appViewModel, scope, drawerState)
        else -> TopBarState(
            navigationIcon = Icons.Outlined.Menu,
            navigationIconDescription = R.string.open_menu,
            title = R.string.home,
            onNavigationIconClick = {}
        )
    }

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
                onQueryChange = {
                    appViewModel.onEvent(AppEvent.SearchBoards(it))
                },
                onBackClick = {
                    appViewModel.onEvent(AppEvent.ShowSearchBar(show = false))
                },
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
                title = { Text(text = stringResource(id = topBarState.title)) },
                actions = {
                    topBarState.actions()
                },
                scrollBehavior = scrollBehavior,
            )
        }
    }
}