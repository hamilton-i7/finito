package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@Composable
fun BottomBar(
    @StringRes fabDescription: Int,
    @StringRes searchDescription: Int,
    showGrid: Boolean = false,
    onSearchClick: () -> Unit = {},
    onChangeLayoutClick: () -> Unit = {}
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(id = searchDescription)
                )
            }
            IconButton(onClick = onChangeLayoutClick) {
                if (showGrid) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = stringResource(id = R.string.grid_view)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.ViewStream,
                        contentDescription = stringResource(id = R.string.list_view)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(id = fabDescription)
                )
            }
        }
    )
}