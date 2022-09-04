package com.example.finito.features.subtasks.domain.repository

import com.example.finito.features.subtasks.domain.entity.Subtask

interface SubtaskRepository {
    suspend fun createMany(vararg subtasks: Subtask)

    suspend fun findAllByTaskId(taskId: Int): List<Subtask>

    suspend fun findOne(id: Int): Subtask?

    suspend fun updateMany(vararg subtasks: Subtask): Int

    suspend fun removeMany(vararg subtasks: Subtask): Int
}