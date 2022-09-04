package com.example.finito.features.subtasks.data.repository

import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.repository.SubtaskRepository

class FakeSubtaskRepository: SubtaskRepository {
    val subtasks = mutableListOf<Subtask>()
    private var subtaskId = 1

    override suspend fun createMany(vararg subtasks: Subtask) {
        this.subtasks.addAll(
            subtasks.map {
                it.copy(subtaskId = subtaskId)
            }.also { subtaskId++ }
        )
    }

    override suspend fun findAllByTaskId(taskId: Int): List<Subtask> {
        return subtasks.filter { it.taskId == taskId }.sortedBy { it.position }
    }

    override suspend fun findOne(id: Int): Subtask? {
        return subtasks.find { it.subtaskId == id }
    }

    override suspend fun updateMany(vararg subtasks: Subtask): Int {
        var updateCount = 0
        val ids = this.subtasks.groupBy { it.subtaskId }
        for (subtask in subtasks) {
            if (ids[subtask.subtaskId] == null) continue
            this.subtasks.set(
                index = subtasks.indexOfFirst { it.subtaskId == subtask.subtaskId },
                element = subtask
            )
            updateCount++
        }
        return updateCount
    }

    override suspend fun removeMany(vararg subtasks: Subtask): Int {
        var deleteCount = 0
        subtasks.toList().forEach {
            this.subtasks.remove(it).also { deleted ->
                if (!deleted) return@also
                deleteCount++
            }
        }
        return deleteCount
    }
}