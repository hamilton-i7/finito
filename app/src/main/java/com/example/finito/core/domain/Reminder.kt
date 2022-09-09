package com.example.finito.core.domain

import androidx.annotation.StringRes
import com.example.finito.R

enum class Reminder(@StringRes val label: Int) {
    FIVE_MINUTES(label = R.string.five_minutes_before),
    TEN_MINUTES(label = R.string.ten_minutes_before),
    FIFTEEN_MINUTES(label = R.string.fifteen_minutes_before),
    THIRTY_MINUTES(label = R.string.thirty_minutes_before)
}