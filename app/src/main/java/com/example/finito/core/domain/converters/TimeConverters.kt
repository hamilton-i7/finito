package com.example.finito.core.domain.converters

import androidx.room.TypeConverter
import java.time.LocalTime

class TimeConverters {
    @TypeConverter
    fun stringToTime(value: String): LocalTime {
        return LocalTime.parse(value)
    }

    @TypeConverter
    fun timeToString(time: LocalTime): String {
        return time.toString()
    }
}