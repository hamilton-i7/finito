package com.example.finito.features.subtasks.data.repository

import com.example.finito.features.subtasks.data.dao.SubtaskDao
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.entity.SubtaskRepository

class SubtaskRepositoryImpl(
    private val dao: SubtaskDao
) : SubtaskRepository {
    override suspend fun createMany(vararg subtasks: Subtask) {
        return dao.createMany(*subtasks)
    }

    override suspend fun updateMany(vararg subtasks: Subtask) {
        return dao.updateMany(*subtasks)
    }

    override suspend fun removeMany(vararg subtasks: Subtask) {
        return dao.removeMany(*subtasks)
    }
}