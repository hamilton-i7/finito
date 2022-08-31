package com.example.finito.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.finito.core.domain.converters.DateConverters
import com.example.finito.core.domain.converters.DateTimeConverters
import com.example.finito.core.domain.converters.TimeConverters
import com.example.finito.features.boards.data.dao.BoardDao
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardLabelCrossRef
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task

@Database(
    entities = [Board::class, Label::class, BoardLabelCrossRef::class, Task::class, Subtask::class],
    version = 1
)
@TypeConverters(DateTimeConverters::class, DateConverters::class, TimeConverters::class)
abstract class FinitoDatabase : RoomDatabase() {

    abstract val boardDao: BoardDao

    companion object {
        const val DATABASE_NAME = "finito_db"
    }
}