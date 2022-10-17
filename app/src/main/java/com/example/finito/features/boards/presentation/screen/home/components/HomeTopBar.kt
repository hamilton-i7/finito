package com.example.finito.features.boards.presentation.screen.home.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.TestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onMenuClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.home)) },
        navigationIcon = { 
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag(TestTags.DRAWER_BUTTON)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = stringResource(id = R.string.open_menu)
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}