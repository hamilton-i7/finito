package com.example.finito.core.presentation.util

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class TabOption(
    val icon: ImageVector,
    @StringRes val contentDescription: Int
)
