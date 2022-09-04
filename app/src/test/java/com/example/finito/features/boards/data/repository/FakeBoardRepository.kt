package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.DetailedTask
import com.example.finito.features.tasks.domain.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBoardRepository : BoardRepository {

    private val boards = mutableListOf<BoardWithLabels>()

    override suspend fun create(board: Board): Long {
        boards.add(BoardWithLabels(
            board = board,
            labels = emptyList()
        ))
        return boards.size + 1L
    }

    fun create(boardWithLabels: BoardWithLabels) {
        boards.add(boardWithLabels)
    }

    override fun findAll(): Flow<List<BoardWithLabels>> {
        return flow {
            emit(boards.filter { !it.board.deleted && !it.board.archived })
        }
    }

    override fun findSimpleBoards(): Flow<List<SimpleBoard>> {
        return flow {
            emit(boards.filter { !it.board.deleted && !it.board.archived }.map {
                SimpleBoard(
                    boardId = it.board.boardId,
                    name = it.board.name
                )
            })
        }
    }

    override fun findArchivedBoards(): Flow<List<BoardWithLabels>> {
        return flow { emit(boards.filter { it.board.archived }) }
    }

    override fun findDeletedBoards(): Flow<List<BoardWithLabels>> {
        return flow { emit(boards.filter { it.board.deleted }) }
    }

    override suspend fun findOne(id: Int): DetailedBoard? {
        val board = boards.find { it.board.boardId == id } ?: return null
        return board.let {
            DetailedBoard(
                board = Board(
                    boardId = it.board.boardId,
                    name = it.board.name,
                    createdAt = it.board.createdAt,
                ),
                tasks = listOf(
                    DetailedTask(
                        task = Task(
                            taskId = 1,
                            boardId = it.board.boardId,
                            name = "Task name",
                            position = 0
                        ),
                        subtasks = emptyList(),
                    ),
                    DetailedTask(
                        task = Task(
                            taskId = 2,
                            boardId = it.board.boardId,
                            name = "Task name",
                            position = 1
                        ),
                        subtasks = listOf(
                            Subtask(subtaskId = 1, name = "Subtask name", taskId = 2),
                            Subtask(subtaskId = 2, name = "Subtask name", taskId = 2),
                        ),
                    ),
                )
            )
        }
    }

    override suspend fun update(board: Board): Int {
        boards.find { it.board.boardId == board.boardId } ?: return 0
        boards.set(
            index = boards.indexOfFirst { it.board.boardId == board.boardId },
            element = BoardWithLabels(board)
        )
        return 1
    }

    override suspend fun remove(board: Board): Int {
        val boardToDeleteIndex = boards.indexOfFirst { it.board.boardId == board.boardId }
        if (boardToDeleteIndex == -1) return 0

        boards.removeAt(boardToDeleteIndex)
        return 1
    }
}