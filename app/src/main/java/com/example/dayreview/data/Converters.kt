package com.example.dayreview.data
import androidx.room.TypeConverter
class Converters {
    @TypeConverter fun fromList(list: List<Boolean>?): String = list?.joinToString(",") { if(it) "1" else "0" } ?: ""
    @TypeConverter fun toList(data: String?): List<Boolean> = data?.split(",")?.map { it == "1" } ?: emptyList()
}
