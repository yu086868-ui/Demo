package com.example.demo.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.example.demo.databinding.FragmentRunningBinding
import com.example.demo.ui.activities.MainActivity
import com.example.demo.data.repository.AchievementRepository

class RunningFragment : Fragment(), AMapLocationListener {

    private var _binding: FragmentRunningBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    private var locationClient: AMapLocationClient? = null

    private var startTime: Long = 0
    private var isRunning = true
    private var isPaused = false
    private var pauseStartTime: Long = 0
    private var totalPauseTime: Long = 0
    private var currentDistance = 0f
    private var currentSpeed = 0f
    private var maxSpeed = 0f

    private val locations = mutableListOf<LatLng>()
    private val polylineOptions = PolylineOptions().width(10f).color(0xFFFF0000.toInt())

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateRunningStats()
            handler.postDelayed(this, 1000)
        }
    }

    private companion object {
        const val TAG = "RunningFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentRunningBinding.inflate(inflater, container, false)

        // 初始化地图
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        try {
            initMap()
            initLocation()
            startTime = System.currentTimeMillis()
            setupClickListeners()
            startUpdatingStats()
        } catch (e: Exception) {
            Log.e(TAG, "初始化失败: ${e.message}", e)
            // 即使初始化失败，也继续运行基础功能
            android.widget.Toast.makeText(requireContext(), "地图功能暂不可用，使用模拟数据", android.widget.Toast.LENGTH_SHORT).show()
            startUpdatingStats()
        }
    }

    private fun initMap() {
        aMap = mapView.map
        aMap.uiSettings.isZoomControlsEnabled = false
        aMap.uiSettings.isMyLocationButtonEnabled = true
        aMap.isMyLocationEnabled = true

        // 设置地图初始位置
        val defaultLocation = LatLng(39.9042, 116.4074)
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
    }

    private fun initLocation() {
        try {
            locationClient = AMapLocationClient(requireContext())
            val option = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                interval = 2000
                isNeedAddress = false
                isOnceLocation = false
            }
            locationClient?.setLocationOption(option)
            locationClient?.setLocationListener(this)

            // 开始定位
            locationClient?.startLocation()
            Log.d(TAG, "定位服务已启动")
        } catch (e: Exception) {
            Log.e(TAG, "定位初始化失败: ${e.message}", e)
            locationClient = null
        }
    }

    private fun setupClickListeners() {
        binding.btnPause.setOnClickListener {
            pauseRunning()
        }

        binding.btnResume.setOnClickListener {
            resumeRunning()
        }

        binding.btnStop.setOnClickListener {
            stopRunning()
        }
    }

    private fun startUpdatingStats() {
        handler.post(updateRunnable)
        // 如果没有定位服务，使用模拟数据
        if (locationClient == null) {
            simulateRunningData()
        }
    }

    private fun pauseRunning() {
        isPaused = true
        pauseStartTime = System.currentTimeMillis()
        binding.btnPause.visibility = View.GONE
        binding.btnResume.visibility = View.VISIBLE

        // 暂停定位
        locationClient?.stopLocation()
    }

    private fun resumeRunning() {
        isPaused = false
        totalPauseTime += System.currentTimeMillis() - pauseStartTime
        binding.btnPause.visibility = View.VISIBLE
        binding.btnResume.visibility = View.GONE

        // 恢复定位
        locationClient?.startLocation()
    }

    private fun stopRunning() {
        if (!isRunning) return // 防止重复调用
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        locationClient?.stopLocation()

        // 更新服务中的数据并停止跑步（保存逻辑在Service中处理）
        val mainActivity = activity as? MainActivity
        mainActivity?.runningService?.let { service ->
            service.updateRunData(currentDistance, currentSpeed, maxSpeed)
            val savedRun = service.stopRun()
            if (savedRun != null) {
                // 更新成就进度
                val runData = AchievementRepository.RunData(
                    distance = savedRun.distance,
                    duration = savedRun.duration,
                    calories = savedRun.calories
                )
                AchievementRepository.updateAchievementProgress(runData)

                android.widget.Toast.makeText(
                    requireContext(),
                    "跑步完成！距离: ${"%.2f".format(savedRun.distance / 1000)}km",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                Log.d(TAG, "跑步完成: ID=${savedRun.id}, 距离=${savedRun.distance}米")
            }
        }

        // 返回主页
        parentFragmentManager.beginTransaction()
            .replace(com.example.demo.R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun onLocationChanged(location: AMapLocation?) {
        if (location != null && location.errorCode == 0 && isRunning && !isPaused) {
            // 定位成功
            val latLng = LatLng(location.latitude, location.longitude)

            // 更新位置到地图中心
            aMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))

            // 绘制轨迹
            if (locations.isNotEmpty()) {
                val lastLocation = locations.last()
                val distance = calculateDistance(lastLocation, latLng)
                currentDistance += distance

                // 更新速度（使用GPS速度，如果可用）
                val gpsSpeed = location.speed
                if (gpsSpeed > 0) {
                    currentSpeed = gpsSpeed
                    if (gpsSpeed > maxSpeed) {
                        maxSpeed = gpsSpeed
                    }
                }
            }

            locations.add(latLng)
            polylineOptions.add(latLng)
            aMap.clear()
            aMap.addPolyline(polylineOptions)

            // 更新服务中的数据
            val mainActivity = activity as? MainActivity
            mainActivity?.runningService?.updateRunData(currentDistance, currentSpeed, maxSpeed)

            updateRunningStats()
        } else if (location != null && location.errorCode != 0) {
            // 定位失败，使用模拟数据
            Log.e(TAG, "定位失败: ${location.errorCode} - ${location.errorInfo}")
            if (locations.isEmpty()) {
                simulateRunningData()
            }
        }
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0]
    }

    private fun updateRunningStats() {
        if (!isRunning) return

        val currentTime = if (isPaused) pauseStartTime else System.currentTimeMillis()
        val elapsedTime = currentTime - startTime - totalPauseTime

        // 更新UI
        binding.tvDuration.text = formatDuration(elapsedTime)
        binding.tvDistance.text = "%.2f".format(currentDistance / 1000)
        binding.tvSpeed.text = "%.1f".format(currentSpeed * 3.6)
        binding.tvCalories.text = "%.0f".format(currentDistance * 0.06f)
    }

    private fun simulateRunningData() {
        // 只在GPS定位失败时使用模拟数据
        Thread {
            while (isRunning && locations.isEmpty()) {
                if (!isPaused) {
                    // 模拟距离增加
                    currentDistance += (currentSpeed * 1.0f).toFloat()

                    // 模拟速度变化
                    val newSpeed = (5f + Math.random() * 7f).toFloat() / 3.6f
                    currentSpeed = newSpeed

                    if (newSpeed * 3.6f > maxSpeed * 3.6f) {
                        maxSpeed = newSpeed
                    }

                    requireActivity().runOnUiThread {
                        if (isRunning && !isPaused) {
                            updateRunningStats()
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (isRunning && !isPaused && locationClient != null) {
            locationClient?.startLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        locationClient?.stopLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        locationClient?.onDestroy()
        mapView.onDestroy()
        _binding = null
        Log.d(TAG, "onDestroyView")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}