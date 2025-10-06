package com.example.demo.data.database.converters

import androidx.room.TypeConverter
import com.example.demo.data.models.LocationPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromLocationPoints(locations: List<LocationPoint>): String {
        return gson.toJson(locations)
    }

    @TypeConverter
    fun toLocationPoints(locationsString: String): List<LocationPoint> {
        if (locationsString.isEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<LocationPoint>>() {}.type
            gson.fromJson(locationsString, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}