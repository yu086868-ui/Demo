package com.example.demo.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.demo.data.database.converters.LocationListConverter

@Entity(tableName = "runs")
@TypeConverters(LocationListConverter::class)
data class DatabaseRun(
    @PrimaryKey
    val id: Long = 0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val distance: Float = 0f,
    val duration: Long = 0L,
    val calories: Float = 0f,
    val averageSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val locations: List<com.example.demo.data.models.LocationPoint> = emptyList()
)