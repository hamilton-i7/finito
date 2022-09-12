package com.example.finito.core.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.CenterTopBar
import com.example.finito.core.presentation.components.bars.MediumTopBar
import com.example.finito.core.presentation.components.bars.TopBar
import com.example.finito.core.presentation.util.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layout(
    appViewModel: AppViewModel = hiltViewModel(),
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val enterOnScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
        topBar = {
            when (currentRoute) {
                Screen.Home.route -> CenterTopBar(
                    appViewModel = appViewModel,
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                    scrollBehavior = enterOnScrollBehavior,
                    searchBarScrollBehavior = pinnedScrollBehavior
                )
                Screen.Archive.route, Screen.Trash.route -> TopBar(
                    appViewModel = appViewModel,
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                    scrollBehavior = enterOnScrollBehavior,
                    searchBarScrollBehavior = pinnedScrollBehavior,
                )
                Screen.Board.route -> MediumTopBar(
                    appViewModel = appViewModel,
                    navController = navController,
                    currentRoute = currentRoute,
                    drawerState = drawerState,
                    scrollBehavior = enterOnScrollBehavior,
                )
            }
        },
        bottomBar = {
            BottomBar(
                appViewModel = appViewModel,
                navController = navController,
                currentRoute = currentRoute
            )
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
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) { content() }
    }
}

