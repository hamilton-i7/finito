package com.example.finito.features.tasks.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.example.finito.features.subtasks.domain.entity.CompletedSubtask
import com.example.finito.features.subtasks.domain.entity.Subtask

data class CompletedTask(
    @ColumnInfo(name = "task_id") val taskId: Int,
    val completed: Boolean = false,
    @Relation(
        parentColumn = "task_id",
        entityColumn = "task_id",
        entity = Subtask::class
    )
    val subtasks: List<CompletedSubtask> = emptyList()
) {
    companion object {
        val dummyTasks = (0..25).map {
            CompletedTask(taskId = it, completed = it % 3 == 0)
        }.shuffled()
    }
}
