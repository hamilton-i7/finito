package com.example.finito

import android.content.Context
import androidx.room.Room
import com.example.finito.core.data.FinitoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFinitoDatabase(@ApplicationContext context: Context): FinitoDatabase {
        return Room.databaseBuilder(
            context,
            FinitoDatabase::class.java,
            FinitoDatabase.DATABASE_NAME)
            .createFromAsset("database/finito.db")
            .build()
    }
}