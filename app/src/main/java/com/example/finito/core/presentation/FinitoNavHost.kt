package com.example.finito.core.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenExitTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenPopEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenPopExitTransition
import com.example.finito.core.presentation.util.NavigationTransitions.peerScreenEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.peerScreenExitTransition
import com.example.finito.features.boards.presentation.addeditboard.AddEditBoardScreen
import com.example.finito.features.boards.presentation.archive.ArchiveScreen
import com.example.finito.features.boards.presentation.board.BoardScreen
import com.example.finito.features.boards.presentation.home.HomeScreen
import com.example.finito.features.boards.presentation.trash.TrashScreen
import com.example.finito.features.tasks.presentation.datetime.TaskDateTimeScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FinitoNavHost(
    navHostController: NavHostController,
    drawerState: DrawerState,
    finishActivity: () -> Unit,
) {
   AnimatedNavHost(
       navController = navHostController,
       startDestination = Screen.Home.route
   ) {
       composable(
           route = Screen.Home.route,
           enterTransition = peerScreenEnterTransition,
           exitTransition = {
               when {
                   Screen.Home.childRoutes.contains(targetState.destination.route) -> {
                       childScreenExitTransition()
                   }
                   else -> peerScreenExitTransition()
               }
           },
           popEnterTransition = {
               when {
                   Screen.Home.childRoutes.contains(initialState.destination.route) -> {
                       childScreenPopEnterTransition()
                   }
                   else -> peerScreenEnterTransition()
               }
           }
       ) {
           HomeScreen(
               navController = navHostController,
               drawerState = drawerState,
               finishActivity = finishActivity
           )
       }

       composable(
           route = Screen.Archive.route,
           enterTransition = peerScreenEnterTransition,
           exitTransition = {
               when {
                   Screen.Archive.childRoutes.contains(targetState.destination.route) -> {
                       childScreenExitTransition()
                   }
                   else -> peerScreenExitTransition()
               }
           },
           popEnterTransition = {
               when {
                   Screen.Archive.childRoutes.contains(initialState.destination.route) -> {
                       childScreenPopEnterTransition()
                   }
                   else -> peerScreenEnterTransition()
               }
           }
       ) {
           ArchiveScreen(
               navController = navHostController,
               drawerState = drawerState,
               finishActivity = finishActivity
           )
       }

       composable(
           route = Screen.Trash.route,
           enterTransition = peerScreenEnterTransition,
           exitTransition = {
               when {
                   Screen.Trash.childRoutes.contains(targetState.destination.route) -> {
                       childScreenExitTransition()
                   }
                   else -> peerScreenExitTransition()
               }
           },
           popEnterTransition = {
               when {
                   Screen.Trash.childRoutes.contains(initialState.destination.route) -> {
                       childScreenPopEnterTransition()
                   }
                   else -> peerScreenEnterTransition()
               }
           }
       ) {
           TrashScreen(
               navController = navHostController,
               drawerState = drawerState,
               finishActivity = finishActivity
           )
       }

       composable(
           route = Screen.Board.route,
           arguments = Screen.Board.arguments,
           enterTransition = {
                when {
                    Screen.Board.parentRoutes.contains(initialState.destination.route) -> {
                        childScreenEnterTransition()
                    }
                    else -> peerScreenEnterTransition()
                }
           },
           popEnterTransition = {
               when {
                   Screen.Board.childRoutes.contains(initialState.destination.route) -> {
                       childScreenPopEnterTransition()
                   }
                   else -> peerScreenEnterTransition()
               }
           },
           exitTransition = peerScreenExitTransition,
           popExitTransition = childScreenPopExitTransition
       ) {
           BoardScreen(
               navController = navHostController,
               drawerState = drawerState,
           )
       }

       composable(
           route = Screen.CreateBoard.route,
           enterTransition = childScreenEnterTransition,
           exitTransition = childScreenExitTransition,
           popExitTransition = childScreenPopExitTransition
       ) {
           AddEditBoardScreen(navController = navHostController)
       }

       composable(
           route = Screen.TaskDateTime.route,
           arguments = Screen.TaskDateTime.arguments,
           enterTransition = childScreenEnterTransition,
           exitTransition = childScreenExitTransition,
           popExitTransition = childScreenPopExitTransition
       ) {
           TaskDateTimeScreen(navController = navHostController)
       }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandleBackPress(
    drawerState: DrawerState? = null,
    onBackPress: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    BackHandler {
        drawerState?.let {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else onBackPress()
        } ?: onBackPress()
    }
}