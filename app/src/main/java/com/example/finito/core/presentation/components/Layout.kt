package com.example.finito.core.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.features.boards.presentation.home.components.HomeTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Layout(
    appViewModel: AppViewModel = hiltViewModel(),
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    val enterOnScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            when (currentRoute) {
                Screen.Home.route -> HomeScreenTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    showSearchBar = appViewModel.showSearchbar,
                    onSearchBarNavigationIconClick = {
                        appViewModel.onEvent(AppEvent.ShowSearchBar(show = false))
                    },
                    searchQuery = appViewModel.searchQuery,
                    onSearchQueryChange = {
                        appViewModel.onEvent(AppEvent.SearchBoards(it))
                    },
                    scrollBehavior = enterOnScrollBehavior,
                    searchBarScrollBehavior = pinnedScrollBehavior
                )
                else -> Unit
            }
        },
        bottomBar = bottomBar@{
            when (currentRoute) {
                Screen.Home.route -> {
                    if (isKeyboardVisible) return@bottomBar
                    BottomBar(
                        fabDescription = R.string.add_board,
                        searchDescription = R.string.search_boards,
                        onChangeLayoutClick = {
                            appViewModel.onEvent(AppEvent.ToggleLayout)
                        },
                        gridLayout = appViewModel.gridLayout,
                        onSearchClick = {
                            appViewModel.onEvent(AppEvent.ShowSearchBar(show = true))
                        },
                        onFabClick = {
                            navController.navigate(route = Screen.CreateBoard.route)
                        }
                    )
                }
                else -> Unit
            }
        },
        modifier = Modifier
            .nestedScroll(
                if (appViewModel.showSearchbar)
                    pinnedScrollBehavior.nestedScrollConnection
                else
                    enterOnScrollBehavior.nestedScrollConnection
            )
            .noRippleClickable { focusManager.clearFocus() },
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) { content() }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopBar(
    onMenuClick: () -> Unit,
    showSearchBar: Boolean,
    onSearchBarNavigationIconClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
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