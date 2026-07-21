package com.chandanshakya.fuellog.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

/**
 * Room type converters for custom types.
 */
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? = timeString?.let { LocalTime.parse(it) }
}
