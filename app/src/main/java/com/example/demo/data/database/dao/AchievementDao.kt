package com.example.demo.data.database.dao

import androidx.room.*
import com.example.demo.data.database.entities.DatabaseAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): kotlinx.coroutines.flow.Flow<List<DatabaseAchievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: DatabaseAchievement)

    @Update
    suspend fun updateAchievement(achievement: DatabaseAchievement)

    @Query("DELETE FROM achievements")
    suspend fun clearAll()
}