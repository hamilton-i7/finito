package com.example.finito.core.presentation.components.bars

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onMenuClick: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.home)) },
        navigationIcon = { 
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = stringResource(id = R.string.open_menu)
                )
            }
        },
        modifier = Modifier.statusBarsPadding()
    )
}