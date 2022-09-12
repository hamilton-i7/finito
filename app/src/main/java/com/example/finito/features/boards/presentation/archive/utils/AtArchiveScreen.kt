package com.example.finito.features.boards.presentation.archive.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.util.TopBarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun atArchiveScreen(scope: CoroutineScope, drawerState: DrawerState): TopBarState {
    return TopBarState(
        navigationIcon = Icons.Outlined.Menu,
        navigationIconDescription = R.string.open_menu,
        title = R.string.archive,
        onNavigationIconClick = {
            scope.launch { drawerState.open() }
        }
    )
}