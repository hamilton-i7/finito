package com.example.finito.core.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.example.finito.features.boards.presentation.archive.ArchiveScreen
import com.example.finito.features.boards.presentation.home.HomeScreen
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
       composable(route = Screen.Home.route) {
           HomeScreen(navHostController = navHostController, drawerState = drawerState)
           HandleBackPress(drawerState, onBackPress =  finishActivity)
       }

       composable(route = Screen.Archive.route) {
           ArchiveScreen(navHostController = navHostController, drawerState = drawerState)
           HandleBackPress(drawerState, onBackPress =  finishActivity)
       }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleBackPress(
    drawerState: DrawerState,
    onBackPress: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else onBackPress()
    }
}