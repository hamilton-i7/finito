package com.example.finito.features.labels.domain.entity

import androidx.room.ColumnInfo
import com.example.finito.core.util.normalize

data class SimpleLabel(
    @ColumnInfo(name = "label_id") val labelId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
)
