package com.example.demo.data.repository

import android.content.Context
import com.example.demo.data.database.AppDatabase
import com.example.demo.data.database.dao.RunDao
import com.example.demo.data.database.entities.DatabaseRun
import com.example.demo.data.models.Run
import com.example.demo.data.models.LocationPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatabaseRepository private constructor(private val runDao: RunDao) {

    // 转换 DatabaseRun 为 Run
    private fun DatabaseRun.toRun(): Run {
        return Run(
            id = this.id,
            startTime = this.startTime,
            endTime = this.endTime,
            distance = this.distance,
            duration = this.duration,
            calories = this.calories,
            averageSpeed = this.averageSpeed,
            maxSpeed = this.maxSpeed,
            locations = this.locations
        )
    }

    // 转换 Run 为 DatabaseRun
    private fun Run.toDatabaseRun(): DatabaseRun {
        return DatabaseRun(
            id = this.id,
            startTime = this.startTime,
            endTime = this.endTime,
            distance = this.distance,
            duration = this.duration,
            calories = this.calories,
            averageSpeed = this.averageSpeed,
            maxSpeed = this.maxSpeed,
            locations = this.locations
        )
    }

    // 获取所有跑步记录（Flow）
    fun getAllRunsFlow(): Flow<List<Run>> {
        return runDao.getAllRuns().map { databaseRuns ->
            databaseRuns.map { it.toRun() }
        }
    }

    // 保存跑步记录
    suspend fun saveRun(run: Run) {
        runDao.insertRun(run.toDatabaseRun())
    }

    // 获取跑步记录数量
    suspend fun getRunCount(): Int {
        return runDao.getRunCount()
    }

    // 获取总距离
    suspend fun getTotalDistance(): Float {
        return runDao.getTotalDistance() ?: 0f
    }

    // 获取总时长
    suspend fun getTotalDuration(): Long {
        return runDao.getTotalDuration() ?: 0L
    }

    // 获取总卡路里
    suspend fun getTotalCalories(): Float {
        return runDao.getTotalCalories() ?: 0f
    }

    // 获取最近记录
    suspend fun getRecentRuns(limit: Int): List<Run> {
        return runDao.getRecentRuns(limit).map { it.toRun() }
    }

    // 删除记录
    suspend fun deleteRun(runId: Long) {
        runDao.deleteRunById(runId)
    }

    companion object {
        @Volatile
        private var INSTANCE: DatabaseRepository? = null

        fun getInstance(context: Context): DatabaseRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getInstance(context)
                val instance = DatabaseRepository(database.runDao())
                INSTANCE = instance
                instance
            }
        }
    }
}