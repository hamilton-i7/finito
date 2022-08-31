package com.example.finito.core.domain.converters

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverters {
    @TypeConverter
    fun stringToDate(value: String): LocalDate {
        return LocalDate.parse(value)
    }

    @TypeConverter
    fun dateToString(date: LocalDate): String {
        return date.toString()
    }
}