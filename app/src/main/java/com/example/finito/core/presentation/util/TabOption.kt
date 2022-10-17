package com.example.finito.core.presentation.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TabOption(
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int
)
