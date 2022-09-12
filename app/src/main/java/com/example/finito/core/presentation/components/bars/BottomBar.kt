package com.example.finito.core.presentation.components.bars

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.AnimationDurationConstants
import com.example.finito.core.presentation.util.BottomBarState
import com.example.finito.features.boards.presentation.archive.utils.ArchiveScreenDefaults
import com.example.finito.features.boards.presentation.home.utils.HomeScreenDefaults

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBar(
    appViewModel: AppViewModel,
    navController: NavController,
    currentRoute: String?,
) {
    val bottomBarState: BottomBarState = when (currentRoute) {
        Screen.Home.route -> {
            HomeScreenDefaults.rememberBottomBarState(
                appViewModel = appViewModel,
                navController = navController,
            )
        }
        Screen.Archive.route -> {
            ArchiveScreenDefaults.rememberBottomBarState(
                appViewModel = appViewModel,
                navController = navController,
            )
        }
        else -> null
    } ?: return

    AnimatedContent(
        targetState = appViewModel.showBottomBar,
        transitionSpec = {
            slideInVertically(
                animationSpec = tween(
                    durationMillis = AnimationDurationConstants.RegularDurationMillis,
                    delayMillis = AnimationDurationConstants.ShortestDurationMillis
                ),
                initialOffsetY = { it / 2 }
            ) with slideOutVertically(
                animationSpec = tween(
                    durationMillis = AnimationDurationConstants.RegularDurationMillis,
                    delayMillis = AnimationDurationConstants.ShortestDurationMillis
                ),
                targetOffsetY = { it / 2 }
            )
        }
    ) { show ->
        if (!show) return@AnimatedContent
        BottomAppBar(
            actions = bottomBarState.actions,
            floatingActionButton = fab@{
                if (!bottomBarState.showFab) return@fab
                FloatingActionButton(
                    onClick = bottomBarState.onFabClick,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = bottomBarState.fabDescription?.let {
                            stringResource(id = it)
                        }
                    )
                }
            }
        )
    }
}