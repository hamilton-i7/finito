package com.example.finito.core.domain.util

import androidx.annotation.StringRes
import com.example.finito.R

sealed class SortingOption(@StringRes val label: Int) {
    sealed class Common(label: Int, val name: String) : SortingOption(label) {
        object NameAZ : Common(R.string.a_z, name = "A_Z")
        object NameZA : Common(R.string.z_a, name = "Z_A")
        object Newest : Common(R.string.newest, name = "NEWEST")
        object Oldest : Common(R.string.oldest, name = "OLDEST")
        object Custom : Common(R.string.custom, name = "CUSTOM")

        companion object {
            val Default = Custom
        }
    }

    sealed class Priority(label: Int, val name: String) : SortingOption(label) {
        object MostUrgent : Priority(R.string.most_urgent, name = "MOST_URGENT")
        object LeastUrgent : Priority(R.string.least_urgent, name = "LEAST_URGENT")
    }
}

val prioritySortingOptions = listOf(
    SortingOption.Priority.MostUrgent,
    SortingOption.Priority.LeastUrgent,
)