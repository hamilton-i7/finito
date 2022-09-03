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

    override suspend fun findAllByTaskId(taskId: Int): List<Subtask> {
        return dao.findAllByTaskId(taskId)
    }

    override suspend fun updateMany(vararg subtasks: Subtask): Int {
        return dao.updateMany(*subtasks)
    }

    override suspend fun removeMany(vararg subtasks: Subtask): Int {
        return dao.removeMany(*subtasks)
    }
}