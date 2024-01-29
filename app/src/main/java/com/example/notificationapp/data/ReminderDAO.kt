package com.example.notificationapp.data

import androidx.room.*
import com.example.notificationapp.models.Reminder

@Dao
interface ReminderDAO {

    @Insert
    fun add(vararg reminders: Reminder)

    @Update
    fun update(reminder: Reminder)

    @Delete
    fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminder")
    fun get(): List<Reminder>

}