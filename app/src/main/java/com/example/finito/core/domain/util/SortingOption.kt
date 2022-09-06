package com.example.finito.core.domain.util

import androidx.annotation.StringRes
import com.example.finito.R

sealed class SortingOption {
    sealed class Common(@StringRes val label: Int) : SortingOption() {
        object NameAZ : Common(R.string.a_z)
        object NameZA : Common(R.string.z_a)
        object Newest : Common(R.string.newest)
        object Oldest : Common(R.string.oldest)
    }

    sealed class Priority(@StringRes val label: Int) : SortingOption() {
        object MostUrgent : Priority(R.string.most_urgent)
        object LeastUrgent : Priority(R.string.least_urgent)
    }
}