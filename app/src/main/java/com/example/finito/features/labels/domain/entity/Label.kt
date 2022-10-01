package com.example.finito.features.labels.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finito.core.domain.util.normalize
import java.time.LocalDateTime

@Entity(tableName = "labels")
data class Label(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "label_id") val labelId: Int = 0,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String = name.normalize(),
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        val dummyLabels  = ('A'..'Z').map {
            Label(name = "Label $it")
        }
    }
}

fun Label.toSimpleLabel(): SimpleLabel {
    return SimpleLabel(
        labelId, name, normalizedName
    )
}
