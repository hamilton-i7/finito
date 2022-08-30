package com.example.finito.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finito.features.boards.data.dao.BoardDao
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.labels.domain.entity.Label

@Database(
    entities = [Board::class, Label::class, BoardLabelCrossRef::class],
    version = 1
)
abstract class FinitoDatabase : RoomDatabase() {

    abstract val boardDao: BoardDao

    companion object {
        const val DATABASE_NAME = "finito_db"
    }
}