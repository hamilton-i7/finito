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
object AppModuleTest {

    @Provides
    @Singleton
    fun provideFinitoDatabase(@ApplicationContext context: Context): FinitoDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            FinitoDatabase::class.java)
            .build()
    }
}