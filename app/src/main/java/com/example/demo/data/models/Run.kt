package com.example.demo.data.models

data class Run(
    var id: Long = 0,
    var startTime: Long = 0L,
    var endTime: Long = 0L,
    var distance: Float = 0f, // 米
    var duration: Long = 0L, // 毫秒
    var calories: Float = 0f,
    var averageSpeed: Float = 0f, // 米/秒
    var maxSpeed: Float = 0f, // 米/秒
    var locations: List<LocationPoint> = emptyList()
)

data class LocationPoint(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var timestamp: Long = 0L,
    var speed: Float = 0f
)