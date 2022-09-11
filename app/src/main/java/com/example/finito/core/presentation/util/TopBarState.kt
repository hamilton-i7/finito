package com.example.finito.core.presentation.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarState(
    val navigationIcon: ImageVector,
    @StringRes val navigationIconDescription: Int,
    val onNavigationIconClick: () -> Unit,
    @StringRes val title: Int,
    val actions: @Composable () -> Unit = {},
)