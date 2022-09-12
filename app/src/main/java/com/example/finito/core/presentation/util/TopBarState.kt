package com.example.finito.core.presentation.util

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarState(
    val navigationIcon: ImageVector,
    @StringRes val navigationIconDescription: Int,
    val onNavigationIconClick: () -> Unit,
    val title: String,
    val actions: @Composable RowScope.() -> Unit = {},
)