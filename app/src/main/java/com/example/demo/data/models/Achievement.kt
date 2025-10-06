package com.example.demo.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val iconResId: Int,
    val type: AchievementType,
    val targetValue: Float,
    var currentValue: Float = 0f,
    var isUnlocked: Boolean = false,
    val unlockDate: Long? = null
) : Parcelable

enum class AchievementType {
    DISTANCE, DURATION, SPEED, COUNT, CALORIES
}