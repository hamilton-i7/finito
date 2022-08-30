package com.example.finito.features.labels.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finito.core.util.normalize
import java.time.LocalDateTime

@Entity(tableName = "labels")
data class Label(
    @PrimaryKey
    @ColumnInfo(name = "label_id") val labelId: Int,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
)
