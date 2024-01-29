package com.example.notificationapp.data

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Date

class Converters {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun toOffsetDateTime(value: String?) : OffsetDateTime? {
        return value?.let{
            return formatter.parse(value,OffsetDateTime::from)
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?) : String?{
        return date?.format(formatter)
    }

}