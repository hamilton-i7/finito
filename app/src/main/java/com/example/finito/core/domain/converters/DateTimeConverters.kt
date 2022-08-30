package com.example.finito.core.domain.converters

import androidx.room.TypeConverter
import java.time.LocalDateTime

class DateTimeConverters {
    @TypeConverter
    fun stringToDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value)
    }

    @TypeConverter
    fun dateTimeToString(dateTime: LocalDateTime): String {
        return dateTime.toString()
    }
}