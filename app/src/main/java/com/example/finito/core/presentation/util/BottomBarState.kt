package com.example.finito.core.presentation.util

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

data class BottomBarState(
    val showFab: Boolean,
    @StringRes val fabDescription: Int? = null,
    val onFabClick: () -> Unit = {},
    val actions: @Composable RowScope.() -> Unit = {},
)
