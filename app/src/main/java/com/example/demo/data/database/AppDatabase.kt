package com.example.demo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.demo.data.database.dao.RunDao
import com.example.demo.data.database.dao.AchievementDao
import com.example.demo.data.database.entities.DatabaseRun
import com.example.demo.data.database.entities.DatabaseAchievement

@Database(
    entities = [DatabaseRun::class, DatabaseAchievement::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao
    abstract fun achievementDao(): AchievementDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "running_app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}