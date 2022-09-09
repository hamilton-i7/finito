package com.example.finito.core.domain

import androidx.annotation.StringRes
import com.example.finito.R

enum class Priority(val level: Int, @StringRes val label: Int) {
    LOW(level = 1, label = R.string.low),
    MEDIUM(level = 2, label = R.string.medium),
    URGENT(level = 3, label = R.string.urgent)
}