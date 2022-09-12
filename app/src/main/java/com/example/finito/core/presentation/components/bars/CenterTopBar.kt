package com.example.finito.core.presentation.components.bars

import androidx.compose.animation.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TopBarState
import com.example.finito.features.boards.presentation.home.components.HomeTopBar
import com.example.finito.features.boards.presentation.home.utils.HomeScreenDefaults

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CenterTopBar(
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    currentRoute: String?,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    searchBarScrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val topBarState: TopBarState?
    val onQueryChange: (TextFieldValue) -> Unit

    when (currentRoute) {
        Screen.Home.route -> {
            topBarState = HomeScreenDefaults.rememberTopBarState(
                appViewModel = appViewModel,
                drawerState = drawerState,
            )
            onQueryChange = {
                appViewModel.onEvent(
                    screen = Screen.Home,
                    event = AppEvent.Home.SearchBoards(it)
                )
            }
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
                onBackClick = topBarState.onNavigationIconClick,
                onQueryChange = onQueryChange,
                scrollBehavior = searchBarScrollBehavior
            )
        } else {
            HomeTopBar(
                onMenuClick = topBarState.onNavigationIconClick,
                scrollBehavior = scrollBehavior
            )
        }
    }
}

