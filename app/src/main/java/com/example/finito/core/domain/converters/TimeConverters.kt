package com.example.finito.core.domain.converters

import androidx.room.TypeConverter
import java.time.LocalTime

class TimeConverters {
    @TypeConverter
    fun stringToTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun timeToString(time: LocalTime?): String? {
        return time?.toString()
    }
}