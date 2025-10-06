package com.example.demo.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class DatabaseAchievement(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val type: String,
    val targetValue: Float,
    val currentValue: Float,
    val isUnlocked: Boolean,
    val iconResId: Int
)