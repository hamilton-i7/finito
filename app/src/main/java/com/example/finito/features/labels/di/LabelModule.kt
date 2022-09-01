package com.example.finito.features.labels.di

import com.example.finito.core.data.FinitoDatabase
import com.example.finito.features.labels.data.repository.LabelRepositoryImpl
import com.example.finito.features.labels.domain.repository.LabelRepository
import com.example.finito.features.labels.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LabelModule {

    @Provides
    @Singleton
    fun provideLabelRepository(db: FinitoDatabase): LabelRepository {
        return LabelRepositoryImpl(db.labelDao)
    }

    @Provides
    @Singleton
    fun provideLabelUseCases(repository: LabelRepository): LabelUseCases {
        return LabelUseCases(
            createLabel = CreateLabel(repository),
            findSimpleLabels = FindSimpleLabels(repository),
            updateLabel = UpdateLabel(repository),
            deleteLabel = DeleteLabel(repository),
        )
    }
}