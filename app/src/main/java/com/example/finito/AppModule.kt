package com.example.finito

import androidx.room.Room
import com.example.finito.core.data.FinitoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFinitoDatabase(app: FinitoApp): FinitoDatabase {
        return Room.databaseBuilder(
            app,
            FinitoDatabase::class.java,
            FinitoDatabase.DATABASE_NAME
        ).createFromAsset("database/kanban.db").build()
    }
}