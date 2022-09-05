package com.example.finito.core.domain.converters

import androidx.room.TypeConverter
import java.time.LocalDateTime

class DateTimeConverters {
    @TypeConverter
    fun stringToDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateTimeToString(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}