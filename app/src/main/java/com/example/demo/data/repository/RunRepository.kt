package com.example.demo.data.repository

import android.content.Context
import com.example.demo.data.models.Run
import com.example.demo.data.persistence.PersistenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RunRepository {
    private val allRuns = mutableListOf<Run>()
    private var persistenceManager: PersistenceManager? = null
    private var isInitialized = false

    // 初始化持久化管理器并加载数据
    fun initialize(context: Context) {
        persistenceManager = PersistenceManager.getInstance(context)

        // 🆕 新增：在后台加载数据库中的数据
        if (!isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                loadRunsFromDatabase()
            }
            isInitialized = true
        }
    }

    // 🆕 新增：从数据库加载数据到内存
    private suspend fun loadRunsFromDatabase() {
        try {
            val databaseRuns = persistenceManager?.getAllRunsFromDatabase() ?: emptyList()
            allRuns.clear()
            allRuns.addAll(databaseRuns)
            android.util.Log.d("RunRepository", "从数据库加载了 ${allRuns.size} 条跑步记录")
        } catch (e: Exception) {
            android.util.Log.e("RunRepository", "从数据库加载数据失败: ${e.message}")
        }
    }

    fun saveRun(run: Run) {
        // 检查是否已存在相同ID的记录
        if (allRuns.any { it.id == run.id }) {
            android.util.Log.w("RunRepository", "重复记录，跳过保存: ID=${run.id}")
            return
        }

        // 为每次跑步生成唯一ID（如果还没有）
        if (run.id == 0L) {
            run.id = System.currentTimeMillis()
        }

        allRuns.add(0, run) // 新的跑步记录放在最前面
        android.util.Log.d("RunRepository", "跑步记录已保存: ID=${run.id}, 距离=${run.distance}米")

        // 保存到数据库
        persistenceManager?.saveRunToDatabase(run)
    }

    // 🆕 新增：获取所有跑步记录（从数据库）
    suspend fun getAllRunsFromDatabase(): List<Run> {
        return persistenceManager?.getAllRunsFromDatabase() ?: emptyList()
    }
    // 在 RunRepository.kt 中添加
    fun saveRunAndUpdateAchievements(run: Run) {
        // 保存跑步记录
        saveRun(run)

        // 更新成就进度
        AchievementRepository.updateAchievementProgress(
            AchievementRepository.RunData(
                distance = run.distance,
                duration = run.duration,
                calories = run.calories
            )
        )

        android.util.Log.d("RunRepository", "已保存跑步记录并更新成就进度")
    }
    // 以下所有现有方法保持不变...
    fun getLatestRun(): Run? {
        return allRuns.firstOrNull()
    }

    fun getAllRuns(): List<Run> {
        return allRuns.toList()
    }

    fun getRunCount(): Int {
        return allRuns.size
    }

    fun getTotalDistance(): Float {
        return allRuns.sumOf { it.distance.toDouble() }.toFloat()
    }

    fun getTotalDuration(): Long {
        return allRuns.sumOf { it.duration.toLong() }
    }

    fun getTotalCalories(): Float {
        return allRuns.sumOf { it.calories.toDouble() }.toFloat()
    }

    fun clearRuns() {
        allRuns.clear()
    }

    fun deleteRun(runId: Long): Boolean {
        val removed = allRuns.removeIf { it.id == runId }
        if (removed) {
            // 🆕 新增：同时从数据库删除
            persistenceManager?.deleteRunFromDatabase(runId)
        }
        return removed
    }

    // 清理重复记录的方法
    fun removeDuplicateRuns() {
        val uniqueRuns = allRuns.distinctBy { it.id }
        val removedCount = allRuns.size - uniqueRuns.size
        if (removedCount > 0) {
            allRuns.clear()
            allRuns.addAll(uniqueRuns)
            android.util.Log.d("RunRepository", "已清理 $removedCount 条重复记录")
        }
    }

    // 检查是否有重复记录
    fun hasDuplicateRuns(): Boolean {
        val runIds = allRuns.map { it.id }
        return runIds.size != runIds.toSet().size
    }
}

private fun PersistenceManager?.deleteRunFromDatabase(runId: Long) {}
