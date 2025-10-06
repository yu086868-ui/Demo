package com.example.demo.data.persistence

import android.content.Context
import com.example.demo.data.models.Run
import com.example.demo.data.repository.DatabaseRepository
import com.example.demo.data.repository.RunRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 数据持久化管理器 - 桥接现有内存存储和数据库存储
 */
class PersistenceManager private constructor(context: Context) {

    private val databaseRepository = DatabaseRepository.getInstance(context)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    companion object {
        @Volatile
        private var INSTANCE: PersistenceManager? = null

        fun getInstance(context: Context): PersistenceManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PersistenceManager(context)
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * 保存跑步记录到数据库（异步）
     */
    fun saveRunToDatabase(run: Run) {
        ioScope.launch {
            try {
                databaseRepository.saveRun(run)
                android.util.Log.d("PersistenceManager", "跑步记录已持久化到数据库: ID=${run.id}")
            } catch (e: Exception) {
                android.util.Log.e("PersistenceManager", "保存到数据库失败: ${e.message}")
            }
        }
    }

    /**
     * 从数据库加载所有记录到内存（可选功能）
     */
    suspend fun loadAllRunsFromDatabase() {
        try {
            val databaseRuns = databaseRepository.getRecentRuns(1000) // 加载最多1000条记录
            // 这里可以选择性地将数据库记录加载到内存，但为了不影响现有逻辑，暂时不自动执行
            android.util.Log.d("PersistenceManager", "从数据库加载了 ${databaseRuns.size} 条记录")
        } catch (e: Exception) {
            android.util.Log.e("PersistenceManager", "从数据库加载失败: ${e.message}")
        }
    }

    /**
     * 获取数据库统计信息
     */
    suspend fun getDatabaseStatistics(): DatabaseStats {
        return try {
            val totalRuns = databaseRepository.getRunCount()
            val totalDistance = databaseRepository.getTotalDistance()
            val totalDuration = databaseRepository.getTotalDuration()
            val totalCalories = databaseRepository.getTotalCalories()

            DatabaseStats(totalRuns, totalDistance, totalDuration, totalCalories)
        } catch (e: Exception) {
            android.util.Log.e("PersistenceManager", "获取数据库统计失败: ${e.message}")
            DatabaseStats(0, 0f, 0L, 0f)
        }
    }

    /**
     * 同步内存数据到数据库（用于应用启动时或需要时）
     */
    fun syncMemoryToDatabase() {
        ioScope.launch {
            try {
                val memoryRuns = RunRepository.getAllRuns()
                memoryRuns.forEach { run ->
                    databaseRepository.saveRun(run)
                }
                android.util.Log.d("PersistenceManager", "已同步 ${memoryRuns.size} 条记录到数据库")
            } catch (e: Exception) {
                android.util.Log.e("PersistenceManager", "同步到数据库失败: ${e.message}")
            }
        }
    }

    /**
     * 从数据库获取所有跑步记录
     */
    suspend fun getAllRunsFromDatabase(): List<Run> {
        return try {
            databaseRepository.getRecentRuns(1000) // 获取最多1000条记录
        } catch (e: Exception) {
            android.util.Log.e("PersistenceManager", "从数据库获取记录失败: ${e.message}")
            emptyList()
        }
    }

    /**
     * 从数据库删除跑步记录
     */
    fun deleteRunFromDatabase(runId: Long) {
        ioScope.launch {
            try {
                databaseRepository.deleteRun(runId)
                android.util.Log.d("PersistenceManager", "从数据库删除记录: ID=$runId")
            } catch (e: Exception) {
                android.util.Log.e("PersistenceManager", "从数据库删除记录失败: ${e.message}")
            }
        }
    }
}

data class DatabaseStats(
    val totalRuns: Int,
    val totalDistance: Float,
    val totalDuration: Long,
    val totalCalories: Float
)