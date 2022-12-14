package com.example.finito.features.labels.data.dao

import androidx.room.*
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.entity.SimpleLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(label: Label)

    @Query("SELECT * FROM labels")
    suspend fun findAll(): List<Label>

    @Query("SELECT label_id, name, normalized_name FROM labels")
    fun findSimpleLabels(): Flow<List<SimpleLabel>>

    @Query("SELECT * FROM labels WHERE label_id = :id")
    suspend fun findOne(id: Int): Label?

    @Update
    suspend fun update(label: Label)

    @Delete
    suspend fun remove(label: Label)
}