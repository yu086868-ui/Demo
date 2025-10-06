package com.example.demo.data.repository

import android.content.Context
import com.example.demo.data.database.AppDatabase
import com.example.demo.data.database.dao.AchievementDao
import com.example.demo.data.database.entities.DatabaseAchievement
import com.example.demo.data.models.Achievement
import com.example.demo.data.models.AchievementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AchievementDatabaseRepository private constructor(private val achievementDao: AchievementDao) {

    // 转换 DatabaseAchievement 为 Achievement
    private fun DatabaseAchievement.toAchievement(): Achievement {
        return Achievement(
            id = this.id,
            name = this.name,
            description = this.description,
            type = AchievementType.valueOf(this.type),
            targetValue = this.targetValue,
            currentValue = this.currentValue,
            isUnlocked = this.isUnlocked,
            iconResId = this.iconResId
        )
    }

    // 转换 Achievement 为 DatabaseAchievement
    private fun Achievement.toDatabaseAchievement(): DatabaseAchievement {
        return DatabaseAchievement(
            id = this.id,
            name = this.name,
            description = this.description,
            type = this.type.name,
            targetValue = this.targetValue,
            currentValue = this.currentValue,
            isUnlocked = this.isUnlocked,
            iconResId = this.iconResId
        )
    }

    suspend fun getAllAchievements(): List<Achievement> {
        return try {
            val databaseAchievements = achievementDao.getAllAchievements().first()
            databaseAchievements.map { it.toAchievement() }
        } catch (e: Exception) {
            android.util.Log.e("AchievementDatabaseRepository", "获取成就失败: ${e.message}")
            emptyList()
        }
    }

    // 保存成就
    suspend fun saveAchievement(achievement: Achievement) {
        try {
            achievementDao.insertAchievement(achievement.toDatabaseAchievement())
        } catch (e: Exception) {
            android.util.Log.e("AchievementDatabaseRepository", "保存成就失败: ${e.message}")
        }
    }

    // 更新成就
    suspend fun updateAchievement(achievement: Achievement) {
        try {
            achievementDao.updateAchievement(achievement.toDatabaseAchievement())
        } catch (e: Exception) {
            android.util.Log.e("AchievementDatabaseRepository", "更新成就失败: ${e.message}")
        }
    }

    // 批量保存成就
    suspend fun saveAllAchievements(achievements: List<Achievement>) {
        try {
            achievements.forEach { achievement ->
                achievementDao.insertAchievement(achievement.toDatabaseAchievement())
            }
        } catch (e: Exception) {
            android.util.Log.e("AchievementDatabaseRepository", "批量保存成就失败: ${e.message}")
        }
    }

    // 检查数据库是否为空
    suspend fun isEmpty(): Boolean {
        return try {
            val achievements = getAllAchievements()
            achievements.isEmpty()
        } catch (e: Exception) {
            true
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AchievementDatabaseRepository? = null

        fun getInstance(context: Context): AchievementDatabaseRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getInstance(context)
                val instance = AchievementDatabaseRepository(database.achievementDao())
                INSTANCE = instance
                instance
            }
        }
    }
}