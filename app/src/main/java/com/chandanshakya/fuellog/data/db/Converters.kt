package com.chandanshakya.fuellog.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
/**
 * Room type converters for custom types.
 */
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }
}
