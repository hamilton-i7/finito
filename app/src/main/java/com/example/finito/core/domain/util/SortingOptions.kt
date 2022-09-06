package com.example.finito.core.domain.util

import androidx.annotation.StringRes
import com.example.finito.R

sealed class SortingOptions {
    sealed class Common(@StringRes val label: Int) : SortingOptions() {
        object A_Z : Common(R.string.a_z)
        object Z_A : Common(R.string.z_a)
        object Newest : Common(R.string.newest)
        object Oldest : Common(R.string.oldest)
    }

    sealed class Priority(@StringRes val label: Int) : SortingOptions() {
        object MostUrgent : Priority(R.string.most_urgent)
        object LeastUrgent : Priority(R.string.least_urgent)
    }
}