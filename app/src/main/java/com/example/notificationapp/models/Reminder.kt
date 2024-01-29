package com.example.notificationapp.models

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.Date

@Entity(tableName = "reminder")
data class Reminder(
    @PrimaryKey(true) @ColumnInfo("id") val id: Int,
    @ColumnInfo("title") var title: String,
    @ColumnInfo("description") var description: String,
    @ColumnInfo("reminder_datetime") var reminderDateTime: OffsetDateTime,
    @ColumnInfo("is_enable") var isReminderEnable: Boolean
    ): Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        TODO("reminderDateTime"),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeByte(if (isReminderEnable) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Reminder> {
        override fun createFromParcel(parcel: Parcel): Reminder {
            return Reminder(parcel)
        }

        override fun newArray(size: Int): Array<Reminder?> {
            return arrayOfNulls(size)
        }
    }
}