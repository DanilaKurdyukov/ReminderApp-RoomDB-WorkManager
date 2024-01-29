package com.example.notificationapp.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notificationapp.models.Reminder

@androidx.room.Database(
    entities = [Reminder::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun reminderDao() : ReminderDAO

    companion object {
        @Volatile
        private var instance: Database? = null

        fun getDb(context: Context): Database{
            if (instance==null){
                synchronized(this){
                    instance = buildDB(context)
                }
            }
            return instance!!
        }

        private fun buildDB(context: Context): Database {
            return Room.databaseBuilder(
                context,
                Database::class.java,
                "todos_database"
            )
                .allowMainThreadQueries()
                .build()
        }
    }

}