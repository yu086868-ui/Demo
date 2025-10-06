package com.example.demo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.databinding.FragmentRunHistoryBinding
import com.example.demo.data.models.Run
import com.example.demo.data.repository.RunRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RunHistoryFragment : Fragment() {

    private var _binding: FragmentRunHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RunHistoryAdapter

    private companion object {
        const val TAG = "RunHistoryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentRunHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        try {
            setupRecyclerView()
            // 🆕 修复：直接加载数据，不要使用延迟
            loadRunHistory()
            updateStats()
        } catch (e: Exception) {
            Log.e(TAG, "初始化失败: ${e.message}", e)
            showError("加载历史记录失败")
        }
    }

    private fun setupRecyclerView() {
        adapter = RunHistoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RunHistoryFragment.adapter
        }
    }

    private fun loadRunHistory() {
        try {
            val runs = RunRepository.getAllRuns()
            Log.d(TAG, "加载跑步记录: ${runs.size} 条")
            adapter.submitList(runs)

            // 显示空状态
            if (runs.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载历史记录失败: ${e.message}", e)
            showError("加载历史记录失败")
        }
    }

    private fun updateStats() {
        try {
            val totalRuns = RunRepository.getRunCount()
            val totalDistance = RunRepository.getTotalDistance()
            val totalDuration = RunRepository.getTotalDuration()
            val totalCalories = RunRepository.getTotalCalories()

            binding.tvTotalRuns.text = totalRuns.toString()
            binding.tvTotalDistance.text = "%.2f".format(totalDistance / 1000)
            binding.tvTotalDuration.text = formatDuration(totalDuration)
            binding.tvTotalCalories.text = "%.0f".format(totalCalories)
        } catch (e: Exception) {
            Log.e(TAG, "更新统计失败: ${e.message}", e)
            showError("更新统计失败")
        }
    }

    // 🆕 新增：显示错误信息
    private fun showError(message: String) {
        try {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.tvEmptyState.text = message
        } catch (e: Exception) {
            Log.e(TAG, "显示错误信息失败: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        // 刷新数据
        loadRunHistory()
        updateStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView")
    }

    inner class RunHistoryAdapter : RecyclerView.Adapter<RunHistoryAdapter.ViewHolder>() {

        private var runs = emptyList<Run>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // 使用 findViewById 确保安全
            private val tvRunDate: android.widget.TextView = itemView.findViewById(com.example.demo.R.id.tvRunDate)
            private val tvDistance: android.widget.TextView = itemView.findViewById(com.example.demo.R.id.tvDistance)
            private val tvDuration: android.widget.TextView = itemView.findViewById(com.example.demo.R.id.tvDuration)
            private val tvPace: android.widget.TextView = itemView.findViewById(com.example.demo.R.id.tvPace)
            private val tvCalories: android.widget.TextView = itemView.findViewById(com.example.demo.R.id.tvCalories)
            private val tvSpeedLevel: android.widget.TextView = itemView.findViewById(com.example.demo.R.id.tvSpeedLevel)

            fun bind(run: Run) {
                try {
                    // 格式化日期
                    val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
                    val dateStr = dateFormat.format(Date(run.startTime))

                    tvRunDate.text = dateStr
                    tvDistance.text = "%.2f".format(run.distance / 1000)
                    tvDuration.text = formatDuration(run.duration)
                    tvPace.text = calculatePace(run.distance, run.duration)
                    tvCalories.text = "%.0f".format(run.calories)

                    // 设置速度等级
                    val speedLevel = getSpeedLevel(run.averageSpeed)
                    tvSpeedLevel.text = speedLevel

                    // 点击项可以查看详情
                    itemView.setOnClickListener {
                        android.widget.Toast.makeText(
                            itemView.context,
                            "跑步详情: ${"%.2f".format(run.distance / 1000)}km, ${formatDuration(run.duration)}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "绑定数据失败: ${e.message}", e)
                    // 设置默认值避免崩溃
                    tvRunDate.text = "日期错误"
                    tvDistance.text = "0.00"
                    tvDuration.text = "00:00:00"
                    tvPace.text = "--:--"
                    tvCalories.text = "0"
                    tvSpeedLevel.text = "未知"
                }
            }

            private fun calculatePace(distance: Float, duration: Long): String {
                return try {
                    if (distance <= 0) return "--:--"
                    val paceSeconds = (duration / 1000f) / (distance / 1000f) // 秒/公里（使用浮点数计算）
                    val minutes = (paceSeconds / 60).toInt() // 转换为整数
                    val seconds = (paceSeconds % 60).toInt() // 转换为整数
                    String.format("%d:%02d", minutes, seconds) // 使用整数格式化
                } catch (e: Exception) {
                    Log.e(TAG, "计算配速失败: ${e.message}", e)
                    "--:--"
                }
            }

            private fun getSpeedLevel(speed: Float): String {
                return try {
                    val speedKmh = speed * 3.6f
                    when {
                        speedKmh < 6 -> "轻松"
                        speedKmh < 8 -> "舒适"
                        speedKmh < 10 -> "中等"
                        speedKmh < 12 -> "快速"
                        else -> "极速"
                    }
                } catch (e: Exception) {
                    "未知"
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(com.example.demo.R.layout.item_run_history, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                holder.bind(runs[position])
            } catch (e: Exception) {
                Log.e(TAG, "绑定ViewHolder失败: ${e.message}", e)
            }
        }

        override fun getItemCount(): Int = runs.size

        fun submitList(newRuns: List<Run>) {
            runs = newRuns
            notifyDataSetChanged()
        }
    }

    private fun formatDuration(millis: Long): String {
        return try {
            val totalSeconds = millis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: Exception) {
            "00:00:00"
        }
    }
}