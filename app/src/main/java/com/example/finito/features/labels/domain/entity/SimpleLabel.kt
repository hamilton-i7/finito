package com.example.finito.features.labels.domain.entity

import androidx.room.ColumnInfo
import com.example.finito.core.domain.util.normalize

data class SimpleLabel(
    @ColumnInfo(name = "label_id") val labelId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
) {
    companion object {
        val dummyLabels = listOf(
            SimpleLabel(labelId = 1, name = "School"),
            SimpleLabel(labelId = 2, name = "Gym"),
            SimpleLabel(labelId = 3, name = "Work"),
            SimpleLabel(labelId = 4, name = "Personal"),
        )
    }
}
