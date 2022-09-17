package com.example.finito.features.boards.di

import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.data.repository.BoardLabelRepositoryImpl
import com.example.finito.features.boards.data.repository.BoardRepositoryImpl
import com.example.finito.features.boards.domain.repository.BoardLabelRepository
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.boards.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BoardModuleTest {

    @Provides
    @Singleton
    fun provideBoardRepository(db: FinitoDatabase): BoardRepository {
        return BoardRepositoryImpl(db.boardDao)
    }

    @Provides
    @Singleton
    fun provideBoardLabelRepository(db: FinitoDatabase): BoardLabelRepository {
        return BoardLabelRepositoryImpl(db.boardLabelDao)
    }

    @Provides
    @Singleton
    fun provideBoardUseCases(
        boardRepository: BoardRepository,
        boardLabelRepository: BoardLabelRepository,
    ): BoardUseCases {
        return BoardUseCases(
            createBoard = CreateBoard(boardRepository, boardLabelRepository),
            findActiveBoards = FindActiveBoards(boardRepository),
            findArchivedBoards = FindArchivedBoards(boardRepository),
            findDeletedBoards = FindDeletedBoards(boardRepository),
            findSimpleBoards = FindSimpleBoards(boardRepository),
            findOneBoard = FindOneBoard(boardRepository),
            updateBoard = UpdateBoard(boardRepository, boardLabelRepository),
            deleteBoard = DeleteBoard(boardRepository),
        )
    }
}