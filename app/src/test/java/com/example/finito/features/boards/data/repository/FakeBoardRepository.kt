package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabels
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.entity.toSimpleLabel
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.DetailedTask
import com.example.finito.features.tasks.domain.entity.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBoardRepository(
    private val labelRepository: FakeLabelRepository,
    private val boardLabelRepository: FakeBoardLabelRepository
) : BoardRepository {
    val boards = mutableListOf<Board>()
    private var boardId = 1

    override suspend fun create(board: Board): Long {
        boards.add(board.copy(boardId = boardId))
        boardId++
        return boardId.toLong()
    }

    override fun findAll(): Flow<List<BoardWithLabels>> {
        return flow {
            emit(
                boards.filter { !it.deleted && !it.archived }.map { board ->
                    val refs = boardLabelRepository.findAllByBoardId(board.boardId)
                    val labels = mutableListOf<SimpleLabel>()
                    refs.forEach { ref ->
                        labelRepository.findOne(ref.labelId)?.also { label ->
                            labels.add(label.toSimpleLabel())
                        }
                    }
                    BoardWithLabels(board, labels)
                }
            )
        }
    }

    override fun findSimpleBoards(): Flow<List<SimpleBoard>> {
        return flow {
            emit(boards.filter { !it.deleted && !it.archived }.map {
                SimpleBoard(
                    boardId = it.boardId,
                    name = it.name
                )
            })
        }
    }

    override fun findArchivedBoards(): Flow<List<BoardWithLabels>> {
        return flow {
            emit(
                boards.filter { it.archived }.map { board ->
                    val refs = boardLabelRepository.findAllByBoardId(board.boardId)
                    val labels = mutableListOf<SimpleLabel>()
                    refs.forEach { ref ->
                        labelRepository.findOne(ref.labelId)?.also { label ->
                            labels.add(label.toSimpleLabel())
                        }
                    }
                    BoardWithLabels(board, labels)
                }
            )
        }
    }

    override fun findDeletedBoards(): Flow<List<BoardWithLabels>> {
        return flow {
            emit(
                boards.filter { it.deleted }.map { board ->
                    val refs = boardLabelRepository.findAllByBoardId(board.boardId)
                    val labels = mutableListOf<SimpleLabel>()
                    refs.forEach { ref ->
                        labelRepository.findOne(ref.labelId)?.also { label ->
                            labels.add(label.toSimpleLabel())
                        }
                    }
                    BoardWithLabels(board, labels)
                }
            )
        }
    }

    override suspend fun findOne(id: Int): DetailedBoard? {
        val board = boards.find { it.boardId == id } ?: return null
        return board.let {
            DetailedBoard(
                board = Board(
                    boardId = it.boardId,
                    name = it.name,
                    createdAt = it.createdAt,
                ),
                tasks = listOf(
                    DetailedTask(
                        task = Task(
                            taskId = 1,
                            boardId = it.boardId,
                            name = "Task name",
                            position = 0
                        ),
                        subtasks = emptyList(),
                    ),
                    DetailedTask(
                        task = Task(
                            taskId = 2,
                            boardId = it.boardId,
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

    override suspend fun update(board: Board) {
        boards.set(
            index = boards.indexOfFirst { it.boardId == board.boardId },
            element = board
        )
    }

    override suspend fun remove(board: Board) {
        boards.remove(board)
    }
}