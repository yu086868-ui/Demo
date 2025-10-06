package com.example.demo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.databinding.FragmentAchievementBinding
import com.example.demo.databinding.ItemAchievementBinding
import com.example.demo.data.models.Achievement
import com.example.demo.data.repository.AchievementRepository

class AchievementFragment : Fragment() {

    private var _binding: FragmentAchievementBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AchievementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadAchievements()
        updateStats()
    }

    private fun setupRecyclerView() {
        adapter = AchievementAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AchievementFragment.adapter
        }
    }

    private fun loadAchievements() {
        val achievements = AchievementRepository.getAllAchievements()
        adapter.submitList(achievements)
    }

    private fun updateStats() {
        val unlockedCount = AchievementRepository.getUnlockedAchievements().size
        val totalCount = AchievementRepository.getAllAchievements().size

        binding.tvAchievementStats.text = "已解锁: $unlockedCount/$totalCount"

        // 计算进度百分比
        val progress = if (totalCount > 0) {
            (unlockedCount.toFloat() / totalCount * 100).toInt()
        } else 0

        binding.progressBar.max = totalCount
        binding.progressBar.progress = unlockedCount
        binding.tvProgress.text = "$progress%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class AchievementAdapter : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

        private var achievements = emptyList<Achievement>()

        inner class ViewHolder(private val itemBinding: ItemAchievementBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(achievement: Achievement) {
                itemBinding.tvAchievementName.text = achievement.name
                itemBinding.tvAchievementDesc.text = achievement.description

                // 设置图标（使用系统图标作为示例）
                itemBinding.ivAchievementIcon.setImageResource(achievement.iconResId)

                if (achievement.isUnlocked) {
                    // 已解锁的成就
                    itemBinding.ivAchievementIcon.alpha = 1.0f
                    itemBinding.tvAchievementName.alpha = 1.0f
                    itemBinding.tvAchievementDesc.alpha = 1.0f
                    itemBinding.progressBar.visibility = View.GONE
                    itemBinding.tvProgress.visibility = View.GONE
                    itemBinding.tvUnlocked.visibility = View.VISIBLE

                    itemBinding.tvUnlocked.text = "已解锁!"
                } else {
                    // 未解锁的成就
                    itemBinding.ivAchievementIcon.alpha = 0.5f
                    itemBinding.tvAchievementName.alpha = 0.5f
                    itemBinding.tvAchievementDesc.alpha = 0.5f
                    itemBinding.progressBar.visibility = View.VISIBLE
                    itemBinding.tvProgress.visibility = View.VISIBLE
                    itemBinding.tvUnlocked.visibility = View.GONE

                    // 计算进度
                    val progress = when (achievement.type) {
                        com.example.demo.data.models.AchievementType.SPEED -> {
                            // 速度类型进度计算（值越小越好）
                            if (achievement.currentValue > 0) {
                                ((achievement.targetValue - achievement.currentValue) / achievement.targetValue * 100).toInt()
                            } else 0
                        }
                        else -> {
                            // 其他类型进度计算
                            (achievement.currentValue / achievement.targetValue * 100).toInt()
                        }
                    }.coerceIn(0, 100)

                    itemBinding.progressBar.progress = progress
                    itemBinding.tvProgress.text = "$progress%"

                    // 显示进度文本
                    val progressText = when (achievement.type) {
                        com.example.demo.data.models.AchievementType.DISTANCE ->
                            "进度: ${"%.1f".format(achievement.currentValue / 1000)}/${achievement.targetValue / 1000}km"
                        com.example.demo.data.models.AchievementType.DURATION ->
                            "进度: ${formatTime(achievement.currentValue.toLong())}/${formatTime(achievement.targetValue.toLong())}"
                        com.example.demo.data.models.AchievementType.SPEED ->
                            "最佳: ${"%.1f".format(achievement.currentValue)}分钟/公里"
                        com.example.demo.data.models.AchievementType.CALORIES ->
                            "进度: ${achievement.currentValue.toInt()}/${achievement.targetValue.toInt()}卡"
                        com.example.demo.data.models.AchievementType.COUNT ->
                            "进度: ${achievement.currentValue.toInt()}/${achievement.targetValue.toInt()}次"
                    }

                    itemBinding.tvProgressDetail.text = progressText
                }
            }

            private fun formatTime(millis: Long): String {
                val minutes = (millis / 60000).toInt()
                return "${minutes}分钟"
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemAchievementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(achievements[position])
        }

        override fun getItemCount(): Int = achievements.size

        fun submitList(newAchievements: List<Achievement>) {
            achievements = newAchievements
            notifyDataSetChanged()
        }
    }
}