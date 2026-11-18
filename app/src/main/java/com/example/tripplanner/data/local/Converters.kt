package com.example.tripplanner.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * SQLite knows only numbers, text and blobs. A converter tells Room
 * how to store a LocalDate (as the number of days since 1970-01-01) and read it back.
 */
class Converters {
    @TypeConverter
    fun dateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)
}