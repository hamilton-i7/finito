package com.example.finito.features.boards.presentation.screen.searchboard.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectModeTopBar(
    selectedAmount: Int,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = selectedAmount.toString()) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = stringResource(id = R.string.go_back)
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = finitoColors.surfaceColorAtElevation(3.dp)
        ),
        actions = {
            TextButton(onClick = onConfirmClick) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        scrollBehavior = scrollBehavior
    )
}