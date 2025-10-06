package com.example.demo.ui.activities

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.demo.R
import com.example.demo.data.repository.AchievementRepository
import com.example.demo.service.RunningService
import com.example.demo.ui.fragments.HomeFragment
import com.example.demo.ui.fragments.RunningFragment
import com.example.demo.data.repository.RunRepository
import com.example.demo.databinding.ActivityMainBinding
import com.example.demo.ui.fragments.AchievementFragment
import com.example.demo.ui.fragments.RunHistoryFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    var runningService: RunningService? = null
    private var isServiceBound = false
    private lateinit var binding: ActivityMainBinding

    private companion object {
        const val TAG = "MainActivity"
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "服务已连接")
            val binder = service as RunningService.RunningBinder
            runningService = binder.getService()
            isServiceBound = true
            observeRunningData()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "服务已断开")
            runningService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "MainActivity已创建")

        RunRepository.initialize(this)
        AchievementRepository.initialize(this)
        Log.d(TAG, "成就系统初始化完成")
        // 设置底部导航
        setupBottomNavigation()

        // 立即请求权限和启动服务
        requestPermissions()

        // 显示主页
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    // 🆕 修改：移除开始跑步功能，只用于页面切换
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.navigation_running -> {
                    // 如果正在跑步，显示跑步界面；否则显示主页的跑步相关功能
                    if (runningService?.isRunning?.value == true) {
                        // 如果正在跑步，切换到跑步界面
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, RunningFragment())
                            .commit()
                    } else {
                        // 如果没有在跑步，切换到首页（首页有开始跑步按钮）
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                        // 选中首页标签
                        binding.bottomNavigation.selectedItemId = R.id.navigation_home
                        // 提示用户在首页开始跑步
                        android.widget.Toast.makeText(
                            this,
                            "请在首页点击开始跑步按钮",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                R.id.navigation_history -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, RunHistoryFragment())
                        .commit()
                    true
                }
                R.id.navigation_achievements -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AchievementFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // 默认选中首页
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun requestPermissions() {
        Log.d(TAG, "请求权限")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "已有定位权限，启动服务")
            startRunningService()
        } else {
            Log.d(TAG, "请求定位权限")
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "权限请求结果: $requestCode, 授权: ${grantResults.all { it == PackageManager.PERMISSION_GRANTED }}")
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startRunningService()
        } else {
            // 权限被拒绝，仍然启动服务（只是没有定位功能）
            startRunningService()
        }
    }

    private fun startRunningService() {
        Log.d(TAG, "启动跑步服务")
        val intent = Intent(this, RunningService::class.java)
        startService(intent) // 先启动服务
        bindService(intent, serviceConnection, BIND_AUTO_CREATE) // 再绑定
    }

    private fun observeRunningData() {
        lifecycleScope.launch {
            runningService?.isRunning?.collect { isRunning ->
                Log.d(TAG, "跑步状态变化: $isRunning")
                if (isRunning) {
                    // 切换到跑步界面
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, RunningFragment())
                        .commit()
                    Log.d(TAG, "已切换到跑步界面")
                    // 更新底部导航选中状态到跑步标签
                    binding.bottomNavigation.selectedItemId = R.id.navigation_running
                } else {
                    // 跑步结束，如果当前在跑步页面，自动回到首页
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment is RunningFragment) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                        binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
    }
}