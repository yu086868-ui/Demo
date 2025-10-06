package com.example.demo.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.demo.data.models.Run
import com.example.demo.data.repository.RunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RunningService : Service() {

    private val binder = RunningBinder()
    private val _isRunning = MutableStateFlow(false)
    private val _currentRun = MutableStateFlow<Run?>(null)

    val isRunning: StateFlow<Boolean> = _isRunning
    val currentRun: StateFlow<Run?> = _currentRun

    private companion object {
        const val TAG = "RunningService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "RunningService已创建")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "服务被绑定")
        return binder
    }

    fun startRun() {
        Log.d(TAG, "startRun被调用")
        _isRunning.value = true
        _currentRun.value = Run(startTime = System.currentTimeMillis())
        Log.d(TAG, "跑步状态已设置为true")
    }

    fun pauseRun() {
        Log.d(TAG, "pauseRun被调用")
        _isRunning.value = false
    }

    fun resumeRun() {
        Log.d(TAG, "resumeRun被调用")
        _isRunning.value = true
    }

    fun stopRun(): Run? {
        Log.d(TAG, "stopRun被调用")
        _isRunning.value = false
        val run = _currentRun.value

        run?.let {
            // 只有在未设置结束时间时才设置（避免重复调用）
            if (it.endTime == 0L) {
                it.endTime = System.currentTimeMillis()
                it.duration = it.endTime - it.startTime

                // 为跑步记录生成唯一ID（如果还没有）
                if (it.id == 0L) {
                    it.id = System.currentTimeMillis()
                }

                // 保存跑步记录（在Service中统一处理，避免重复）
                RunRepository.saveRun(it)
                Log.d(TAG, "跑步记录已保存: ID=${it.id}, 距离=${it.distance}米, 时长=${it.duration}毫秒")
            } else {
                Log.d(TAG, "跑步记录已存在，跳过保存: ID=${it.id}")
            }
        }

        _currentRun.value = null
        return run
    }

    // 只更新数据，不保存
    fun updateRunData(distance: Float, averageSpeed: Float, maxSpeed: Float) {
        val currentRun = _currentRun.value ?: return
        currentRun.distance = distance
        currentRun.averageSpeed = averageSpeed
        currentRun.maxSpeed = maxSpeed
        currentRun.calories = distance * 0.06f // 简单计算卡路里
        _currentRun.value = currentRun
        Log.d(TAG, "跑步数据已更新: 距离=$distance, 平均速度=$averageSpeed")
    }

    inner class RunningBinder : Binder() {
        fun getService(): RunningService = this@RunningService
    }
}