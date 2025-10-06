package com.example.demo.data.repository

import android.content.Context
import com.example.demo.data.models.Achievement
import com.example.demo.data.models.AchievementType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AchievementRepository {
    private val achievements = mutableListOf<Achievement>()
    private var isInitialized = false
    private var databaseRepository: AchievementDatabaseRepository? = null

    // 初始化数据库
    fun initialize(context: Context) {
        databaseRepository = AchievementDatabaseRepository.getInstance(context)

        // 在后台加载成就数据
        CoroutineScope(Dispatchers.IO).launch {
            loadAchievementsFromDatabase()
        }
    }

    // 从数据库加载成就
    private suspend fun loadAchievementsFromDatabase() {
        try {
            val databaseAchievements = databaseRepository?.getAllAchievements() ?: emptyList()

            if (databaseAchievements.isNotEmpty()) {
                // 数据库中有数据，使用数据库中的数据
                achievements.clear()
                achievements.addAll(databaseAchievements)
                isInitialized = true
                android.util.Log.d("AchievementRepository", "从数据库加载了 ${achievements.size} 个成就")
            } else {
                // 数据库为空，初始化默认成就
                initializeDefaultAchievements()
                android.util.Log.d("AchievementRepository", "初始化了默认成就")
            }
        } catch (e: Exception) {
            android.util.Log.e("AchievementRepository", "从数据库加载成就失败: ${e.message}")
            // 如果加载失败，也初始化默认成就
            initializeDefaultAchievements()
        }
    }

    // 初始化默认成就
    private suspend fun initializeDefaultAchievements() {
        achievements.clear()

        // 距离相关成就
        achievements.addAll(listOf(
            Achievement(
                id = 1,
                name = "初出茅庐",
                description = "完成第一次跑步",
                iconResId = android.R.drawable.ic_menu_compass,
                type = AchievementType.COUNT,
                targetValue = 1f
            ),
            Achievement(
                id = 2,
                name = "1公里跑者",
                description = "单次跑步达到1公里",
                iconResId = android.R.drawable.ic_menu_directions,
                type = AchievementType.DISTANCE,
                targetValue = 1000f
            ),
            Achievement(
                id = 3,
                name = "5公里挑战",
                description = "单次跑步达到5公里",
                iconResId = android.R.drawable.ic_menu_mylocation,
                type = AchievementType.DISTANCE,
                targetValue = 5000f
            ),
            Achievement(
                id = 4,
                name = "10公里大师",
                description = "单次跑步达到10公里",
                iconResId = android.R.drawable.ic_dialog_map,
                type = AchievementType.DISTANCE,
                targetValue = 10000f
            ),
            Achievement(
                id = 5,
                name = "半程马拉松",
                description = "单次跑步达到21公里",
                iconResId = android.R.drawable.star_big_on,
                type = AchievementType.DISTANCE,
                targetValue = 21000f
            )
        ))

        // 时长相关成就
        achievements.addAll(listOf(
            Achievement(
                id = 6,
                name = "坚持15分钟",
                description = "单次跑步达到15分钟",
                iconResId = android.R.drawable.ic_lock_idle_alarm,
                type = AchievementType.DURATION,
                targetValue = 15 * 60 * 1000f
            ),
            Achievement(
                id = 7,
                name = "半小时跑者",
                description = "单次跑步达到30分钟",
                iconResId = android.R.drawable.ic_menu_agenda,
                type = AchievementType.DURATION,
                targetValue = 30 * 60 * 1000f
            ),
            Achievement(
                id = 8,
                name = "一小时战士",
                description = "单次跑步达到60分钟",
                iconResId = android.R.drawable.ic_menu_today,
                type = AchievementType.DURATION,
                targetValue = 60 * 60 * 1000f
            )
        ))

        // 速度相关成就
        achievements.addAll(listOf(
            Achievement(
                id = 9,
                name = "配速达人",
                description = "平均配速达到8分钟/公里",
                iconResId = android.R.drawable.ic_menu_sort_by_size,
                type = AchievementType.SPEED,
                targetValue = 8f
            ),
            Achievement(
                id = 10,
                name = "速度之星",
                description = "平均配速达到6分钟/公里",
                iconResId = android.R.drawable.star_big_on,
                type = AchievementType.SPEED,
                targetValue = 6f
            ),
            Achievement(
                id = 11,
                name = "飞毛腿",
                description = "平均配速达到5分钟/公里",
                iconResId = android.R.drawable.ic_menu_set_as,
                type = AchievementType.SPEED,
                targetValue = 5f
            )
        ))

        // 卡路里相关成就
        achievements.addAll(listOf(
            Achievement(
                id = 12,
                name = "燃烧100卡",
                description = "单次跑步消耗100卡路里",
                iconResId = android.R.drawable.ic_lock_power_off,
                type = AchievementType.CALORIES,
                targetValue = 100f
            ),
            Achievement(
                id = 13,
                name = "热量战士",
                description = "单次跑步消耗300卡路里",
                iconResId = android.R.drawable.ic_dialog_email,
                type = AchievementType.CALORIES,
                targetValue = 300f
            )
        ))

        // 次数相关成就
        achievements.addAll(listOf(
            Achievement(
                id = 14,
                name = "跑步爱好者",
                description = "累计完成5次跑步",
                iconResId = android.R.drawable.ic_media_play,
                type = AchievementType.COUNT,
                targetValue = 5f
            ),
            Achievement(
                id = 15,
                name = "跑步达人",
                description = "累计完成20次跑步",
                iconResId = android.R.drawable.ic_media_pause,
                type = AchievementType.COUNT,
                targetValue = 20f
            ),
            Achievement(
                id = 16,
                name = "跑步大师",
                description = "累计完成50次跑步",
                iconResId = android.R.drawable.ic_media_ff,
                type = AchievementType.COUNT,
                targetValue = 50f
            )
        ))

        isInitialized = true

        // 保存默认成就到数据库
        saveAllAchievementsToDatabase()
    }

    // 保存所有成就到数据库
    private suspend fun saveAllAchievementsToDatabase() {
        try {
            databaseRepository?.saveAllAchievements(achievements)
            android.util.Log.d("AchievementRepository", "保存了 ${achievements.size} 个成就到数据库")
        } catch (e: Exception) {
            android.util.Log.e("AchievementRepository", "保存成就到数据库失败: ${e.message}")
        }
    }

    // 保存单个成就到数据库
    private fun saveAchievementToDatabase(achievement: Achievement) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                databaseRepository?.updateAchievement(achievement)
                android.util.Log.d("AchievementRepository", "保存成就: ${achievement.name}")
            } catch (e: Exception) {
                android.util.Log.e("AchievementRepository", "保存成就到数据库失败: ${e.message}")
            }
        }
    }

    fun getAllAchievements(): List<Achievement> {
        // 如果还没初始化，使用旧的初始化方法（兼容性）
        if (!isInitialized) {
            initializeLegacyAchievements()
        }
        return achievements.toList()
    }

    // 旧的初始化方法（保持兼容）
    private fun initializeLegacyAchievements() {
        if (isInitialized) return
        // 这里可以调用 initializeDefaultAchievements 但需要同步执行
        // 简化处理，直接设置标志位
        isInitialized = true
    }

    fun getUnlockedAchievements(): List<Achievement> {
        return getAllAchievements().filter { it.isUnlocked }
    }

    fun getLockedAchievements(): List<Achievement> {
        return getAllAchievements().filter { !it.isUnlocked }
    }

    fun updateAchievementProgress(runData: RunData) {
        val allAchievements = getAllAchievements()

        allAchievements.forEach { achievement ->
            val originalUnlocked = achievement.isUnlocked
            val originalValue = achievement.currentValue

            when (achievement.type) {
                AchievementType.DISTANCE -> {
                    achievement.currentValue += runData.distance
                }
                AchievementType.DURATION -> {
                    achievement.currentValue += runData.duration
                }
                AchievementType.SPEED -> {
                    val pace = if (runData.distance > 0) {
                        (runData.duration / 60000f) / (runData.distance / 1000f)
                    } else 0f
                    if (pace > 0 && (achievement.currentValue == 0f || pace < achievement.currentValue)) {
                        achievement.currentValue = pace
                    }
                }
                AchievementType.CALORIES -> {
                    achievement.currentValue += runData.calories
                }
                AchievementType.COUNT -> {
                    achievement.currentValue += 1f
                }
            }

            // 检查是否解锁成就
            if (!achievement.isUnlocked) {
                val shouldUnlock = when (achievement.type) {
                    AchievementType.SPEED -> achievement.currentValue <= achievement.targetValue && achievement.currentValue > 0
                    else -> achievement.currentValue >= achievement.targetValue
                }

                if (shouldUnlock) {
                    achievement.isUnlocked = true
                    android.util.Log.d("AchievementRepository", "成就解锁: ${achievement.name}")
                }
            }

            // 如果成就状态有变化，保存到数据库
            if (achievement.isUnlocked != originalUnlocked || achievement.currentValue != originalValue) {
                saveAchievementToDatabase(achievement)
            }
        }
    }

    data class RunData(
        val distance: Float = 0f,
        val duration: Long = 0L,
        val calories: Float = 0f
    )

    fun resetAchievements() {
        achievements.forEach {
            it.currentValue = 0f
            it.isUnlocked = false
        }

        // 重置数据库
        CoroutineScope(Dispatchers.IO).launch {
            try {
                initializeDefaultAchievements() // 这会重新保存默认成就
            } catch (e: Exception) {
                android.util.Log.e("AchievementRepository", "重置成就失败: ${e.message}")
            }
        }
    }

    // 🆕 新增：强制重新加载数据（用于调试）
    fun reloadFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            loadAchievementsFromDatabase()
        }
    }
}