package com.example.finito.features.boards.presentation.trash.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.domain.util.menu.TrashScreenMenuOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.MENU_MIN_WIDTH
import com.example.finito.core.presentation.util.DialogType
import com.example.finito.core.presentation.util.TopBarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun atTrashScreen(
    appViewModel: AppViewModel,
    scope: CoroutineScope,
    drawerState: DrawerState,
): TopBarState {
    val menuOptions = listOf<TrashScreenMenuOption>(TrashScreenMenuOption.EmptyTrash)

    return TopBarState(
        navigationIcon = Icons.Outlined.Menu,
        navigationIconDescription = R.string.open_menu,
        title = R.string.trash,
        onNavigationIconClick = {
            scope.launch { drawerState.open() }
        },
        actions = {
            Box {
                IconButton(onClick = {
                    appViewModel.onEvent(AppEvent.ShowTopBarMenu(show = true))
                }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = appViewModel.showTopBarMenu,
                    onDismissRequest = {
                        appViewModel.onEvent(AppEvent.ShowTopBarMenu(show = false))
                    },
                    modifier = Modifier.widthIn(min = MENU_MIN_WIDTH)
                ) {
                    menuOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(id = option.label)) },
                            onClick = {
                                appViewModel.onEvent(AppEvent.ShowTopBarMenu(show = false))
                                when (option) {
                                    TrashScreenMenuOption.EmptyTrash -> {
                                        appViewModel.onEvent(AppEvent.ShowDialog(
                                            type = DialogType.EmptyTrash
                                        ))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}