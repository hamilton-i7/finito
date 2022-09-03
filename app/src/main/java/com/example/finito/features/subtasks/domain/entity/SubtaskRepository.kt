package com.example.finito.features.subtasks.domain.entity

interface SubtaskRepository {
    suspend fun createMany(vararg subtasks: Subtask)

    suspend fun updateMany(vararg subtasks: Subtask)

    suspend fun removeMany(vararg subtasks: Subtask)
}