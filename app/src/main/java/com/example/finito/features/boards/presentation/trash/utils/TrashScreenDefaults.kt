package com.example.finito.features.boards.presentation.trash.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.domain.util.menu.TrashScreenMenuOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.util.DialogType
import com.example.finito.core.presentation.util.TopBarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object TrashScreenDefaults {
    private val menuOptions = listOf<TrashScreenMenuOption>(TrashScreenMenuOption.EmptyTrash)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun rememberTopBarState(
        appViewModel: AppViewModel,
        drawerState: DrawerState,
        scope: CoroutineScope = rememberCoroutineScope(),
    ): TopBarState {
        val trashTitle = stringResource(id = R.string.trash)

        return remember(scope, drawerState, appViewModel) {
            TopBarState(
                navigationIcon = Icons.Outlined.Menu,
                navigationIconDescription = R.string.open_menu,
                title = trashTitle,
                onNavigationIconClick = {
                    scope.launch { drawerState.open() }
                },
                actions = {
                    Box {
                        IconButton(onClick = {
                            appViewModel.onEvent(
                                screen = Screen.Trash,
                                event = AppEvent.Trash.ShowTopBarMenu(show = true)
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(id = R.string.more_options)
                            )
                        }
                        FinitoMenu(
                            show = appViewModel.showTopBarMenu,
                            onDismiss = {
                                appViewModel.onEvent(
                                    screen = Screen.Trash,
                                    event = AppEvent.Trash.ShowTopBarMenu(show = false)
                                )
                            },
                            options = menuOptions,
                            onOptionClick = {
                                appViewModel.onEvent(
                                    screen = Screen.Trash,
                                    event = AppEvent.Trash.ShowTopBarMenu(show = false)
                                )
                                when (it) {
                                    TrashScreenMenuOption.EmptyTrash -> {
                                        appViewModel.onEvent(
                                            screen = Screen.Trash,
                                            event = AppEvent.Trash.ShowDialog(
                                                type = DialogType.EmptyTrash
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}