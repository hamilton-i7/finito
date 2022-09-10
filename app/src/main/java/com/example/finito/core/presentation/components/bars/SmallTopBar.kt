package com.example.finito.core.presentation.components.bars

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopBar(
    onMenuClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    @StringRes title: Int,
) {
    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = stringResource(id = R.string.open_menu)
                )
            }
        },
        scrollBehavior = scrollBehavior)
}