package com.example.finito.features.boards.domain.entity

import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import java.time.LocalDateTime

data class DetailedBoard(
    val boardId: Int = 0,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val tasks: List<TaskWithSubtasks> = emptyList()
)
