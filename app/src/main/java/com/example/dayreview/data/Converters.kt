package com.example.dayreview.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromHistoryList(history: List<Boolean>?): String {
        return history?.joinToString(",") { if (it) "1" else "0" } ?: ""
    }

    @TypeConverter
    fun toHistoryList(data: String?): List<Boolean> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").map { it == "1" }
    }
}
