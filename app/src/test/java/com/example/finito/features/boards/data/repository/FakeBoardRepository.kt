package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.labels.data.repository.FakeLabelRepository
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.entity.toSimpleLabel
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBoardRepository(
    private val labelRepository: FakeLabelRepository,
    private val boardLabelRepository: FakeBoardLabelRepository
) : BoardRepository {
    private val boards = mutableListOf<Board>()
    private var boardId = 1

    override suspend fun create(board: Board): Long {
        boards.add(board.copy(boardId = boardId))
        boardId++
        return boardId.toLong()
    }

    override suspend fun findAll(): List<Board> {
        return boards.toList()
    }

    override fun findActiveBoards(): Flow<List<BoardWithLabelsAndTasks>> {
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
                    BoardWithLabelsAndTasks(board, labels)
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

    override fun findArchivedBoards(): Flow<List<BoardWithLabelsAndTasks>> {
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
                    BoardWithLabelsAndTasks(board, labels)
                }
            )
        }
    }

    override fun findDeletedBoards(): Flow<List<BoardWithLabelsAndTasks>> {
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
                    BoardWithLabelsAndTasks(board, labels)
                }
            )
        }
    }

    override fun findOne(id: Int): Flow<DetailedBoard?> {
        return flow {
            val board = boards.find { it.boardId == id } ?: run {
                emit(null)
                return@flow
            }
            emit(
                board.let {
                    DetailedBoard(
                        board = Board(
                            boardId = it.boardId,
                            name = it.name,
                            createdAt = it.createdAt,
                        ),
                        tasks = listOf(
                            TaskWithSubtasks(
                                task = Task(
                                    taskId = 1,
                                    boardId = it.boardId,
                                    name = "Task name",
                                    boardPosition = 0
                                ),
                                subtasks = emptyList(),
                            ),
                            TaskWithSubtasks(
                                task = Task(
                                    taskId = 2,
                                    boardId = it.boardId,
                                    name = "Task name",
                                    boardPosition = 1
                                ),
                                subtasks = listOf(
                                    Subtask(subtaskId = 1, name = "Subtask name", taskId = 2),
                                    Subtask(subtaskId = 2, name = "Subtask name", taskId = 2),
                                ),
                            ),
                        )
                    )
                }
            )
        }
    }

    override suspend fun update(board: Board) {
        boards.set(
            index = boards.indexOfFirst { it.boardId == board.boardId },
            element = board
        )
    }

    override suspend fun remove(vararg boards: Board) {
        this.boards.removeAll(boards.toSet())
    }
}