package com.example.finito.features.boards.di

import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.boards.data.repository.BoardRepositoryImpl
import com.example.finito.features.boards.domain.repository.BoardRepository
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.domain.usecase.GetBoardsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BoardModule {

    @Provides
    @Singleton
    fun provideBoardRepository(db: FinitoDatabase): BoardRepository {
        return BoardRepositoryImpl(db.boardDao)
    }

    @Provides
    @Singleton
    fun provideBoardUseCases(repository: BoardRepository): BoardUseCases {
        return BoardUseCases(
            getBoardsUseCase = GetBoardsUseCase(repository)
        )
    }
}