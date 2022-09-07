package com.example.finito.core.domain.converters

import androidx.room.TypeConverter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimeConverters {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    @TypeConverter
    fun stringToTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, formatter) }
    }

    @TypeConverter
    fun timeToString(time: LocalTime?): String? {
        return time?.format(formatter)
    }
}