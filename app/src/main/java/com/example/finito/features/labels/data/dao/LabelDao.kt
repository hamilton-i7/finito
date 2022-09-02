package com.example.finito.features.labels.data.dao

import androidx.room.*
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.SimpleLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(label: Label)

    @Query("SELECT label_id, name, normalized_name FROM labels")
    fun findSimpleLabels(): Flow<List<SimpleLabel>>

    @Update
    suspend fun update(label: Label): Int

    @Delete
    suspend fun remove(label: Label): Int
}