package com.example.finito.core.presentation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import com.example.finito.core.presentation.components.Drawer
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun App(finishActivity: () -> Unit) {
    val navController = rememberAnimatedNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    Drawer(
        drawerState = drawerState,
        isSelectedScreen = { false }
    ) {
        FinitoNavHost(navController, drawerState, finishActivity)
    }
}