package com.example.demo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.demo.R
import com.example.demo.data.repository.RunRepository
import com.example.demo.databinding.FragmentHomeBinding
import com.example.demo.ui.activities.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private companion object {
        const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: Fragment视图已创建")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: 视图已创建完成")

        // 显示跑步统计数据
        updateRunStats()

        // 🎯 保持原有的按钮点击逻辑不变
        binding.btnStartRun.setOnClickListener {
            Log.d(TAG, "开始跑步按钮被点击")
            startRunning()
        }

        binding.btnAchievements.setOnClickListener {
            Log.d(TAG, "成就按钮被点击")
            // 跳转到成就页面
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AchievementFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnRunHistory.setOnClickListener {
            Log.d(TAG, "历史记录按钮被点击")
            // 跳转到跑步历史页面
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RunHistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        Log.d(TAG, "按钮点击监听器已设置")
    }

    override fun onResume() {
        super.onResume()
        // 每次回到主页时更新数据
        updateRunStats()
    }

    private fun updateRunStats() {
        val latestRun = RunRepository.getLatestRun()
        val totalRuns = RunRepository.getRunCount()
        val totalDistance = RunRepository.getTotalDistance()
        val totalDuration = RunRepository.getTotalDuration()

        // 显示最近单次跑步
        if (latestRun != null) {
            binding.tvLastDistance.text = "%.2f".format(latestRun.distance / 1000)
            binding.tvLastDuration.text = formatDuration(latestRun.duration)
            binding.tvLastDate.text = "最近跑步"
            Log.d(TAG, "显示最近跑步数据: 距离=${latestRun.distance}米, 时长=${latestRun.duration}毫秒")
        } else {
            binding.tvLastDistance.text = "0.00"
            binding.tvLastDuration.text = "00:00:00"
            binding.tvLastDate.text = "最近跑步"
            Log.d(TAG, "没有最近跑步数据")
        }

        // 显示累计统计
        binding.tvTotalRuns.text = totalRuns.toString()
        binding.tvTotalDistance.text = "%.2f".format(totalDistance / 1000)
        binding.tvTotalDuration.text = formatDuration(totalDuration)
    }

    private fun startRunning() {
        val mainActivity = activity as? MainActivity
        if (mainActivity != null) {
            if (mainActivity.runningService != null) {
                Log.d(TAG, "通过绑定的服务启动跑步")
                mainActivity.runningService!!.startRun()
            } else {
                Log.d(TAG, "服务未绑定，直接启动服务")
                val intent = android.content.Intent(requireContext(), com.example.demo.service.RunningService::class.java)
                requireContext().startService(intent)

                binding.btnStartRun.postDelayed({
                    if (mainActivity.runningService != null) {
                        mainActivity.runningService!!.startRun()
                    } else {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "服务启动中，请再次点击开始跑步",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }, 1000)
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView: 视图已销毁")
    }
}