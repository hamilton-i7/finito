package com.example.finito.features.boards.data.repository

import com.example.finito.features.boards.domain.entity.*
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.labels.domain.entity.Label
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBoardRepository : BoardRepository {

    private val boards = mutableListOf<BoardWithLabels>()

    val boardLabels = mutableListOf<BoardLabelCrossRef>()

    override suspend fun create(board: Board) {
        boards.add(BoardWithLabels(
            board = board,
            labels = emptyList()
        ))
    }

    override suspend fun create(board: Board, labels: List<BoardLabelCrossRef>) {
        boards.add(
            BoardWithLabels(
                board = board,
                labels = labels.map { Label(it.labelId, name = "Label name") }
            )
        )
    }

    override suspend fun create(vararg labels: BoardLabelCrossRef) {
        boardLabels.addAll(labels)
    }

    override fun findAll(): Flow<List<BoardWithLabels>> {
        return flow { emit(boards.filter { !it.board.deleted && !it.board.archived }) }
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
        TODO("Not yet implemented")
    }

    override suspend fun update(board: Board) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(board: Board) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(vararg labels: BoardLabelCrossRef) {
        TODO("Not yet implemented")
    }
}