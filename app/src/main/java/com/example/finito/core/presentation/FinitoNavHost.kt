package com.example.finito.core.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.example.finito.core.presentation.components.util.NavigationTransitions.dialogScreenEnterTransition
import com.example.finito.core.presentation.components.util.NavigationTransitions.dialogScreenExistTransition
import com.example.finito.core.presentation.components.util.NavigationTransitions.mainScreenEnterTransition
import com.example.finito.core.presentation.components.util.NavigationTransitions.mainScreenExitTransition
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
           enterTransition = mainScreenEnterTransition,
           exitTransition = mainScreenExitTransition
       ) {
           HomeScreen(
               navHostController = navHostController,
               drawerState = drawerState,
               finishActivity = finishActivity
           )
       }

       composable(
           route = Screen.Archive.route,
           enterTransition = mainScreenEnterTransition,
           exitTransition = mainScreenExitTransition
       ) {
           ArchiveScreen(
               navHostController = navHostController,
               drawerState = drawerState,
               finishActivity = finishActivity
           )
       }

       composable(
           route = Screen.Trash.route,
           enterTransition = mainScreenEnterTransition,
           exitTransition = mainScreenExitTransition
       ) {
           TrashScreen(
               navHostController = navHostController,
               drawerState = drawerState,
               finishActivity = finishActivity
           )
       }

       composable(
           route = Screen.Board.route,
           arguments = Screen.Board.arguments,
           enterTransition = mainScreenEnterTransition,
           exitTransition = mainScreenExitTransition,
           popEnterTransition = null
       ) {
           BoardScreen(
               navController = navHostController,
               drawerState = drawerState,
               finishActivity = finishActivity
           )
       }

       composable(
           route = Screen.TaskDateTime.route,
           arguments = Screen.TaskDateTime.arguments,
           enterTransition = dialogScreenEnterTransition,
           exitTransition = dialogScreenExistTransition
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