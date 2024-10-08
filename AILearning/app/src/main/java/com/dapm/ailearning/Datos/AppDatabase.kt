package com.dapm.ailearning.Datos

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [Leccion::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leccionDao(): LeccionDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AiLearning"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
