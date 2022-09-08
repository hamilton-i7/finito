package com.example.finito.features.tasks.domain.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.subtasks.domain.entity.Subtask

data class DetailedTask(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "board_id",
        entityColumn = "board_id",
        entity = Board::class
    )
    val board: SimpleBoard,
    @Relation(
        parentColumn = "task_id",
        entityColumn = "task_id",
    )
    val subtasks: List<Subtask> = emptyList()
) {
    companion object {
        val dummyTasks = ('A'..'Z').mapIndexed { index, _ ->
            DetailedTask(
                task = Task.dummyTasks.random(),
                board = Board.dummyBoards.random().let {
                    SimpleBoard(boardId = it.boardId, name = it.name)
                },
                subtasks = Subtask.dummySubtasks.take(index)
            )
        }
    }
}
