package com.example.dayreview.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean,
    val date: String,
    val time: String? = null // New Field: "14:30" or null
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val colorArgb: Int,
    val streak: Int = 0,
    val isDoneToday: Boolean = false,
    val history: List<Boolean> = emptyList()
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val date: String, 
    val moodId: Int
)

@Entity(tableName = "mood_config")
data class MoodConfigEntity(
    @PrimaryKey val id: Int,
    val label: String,
    val colorArgb: Int,
    val iconResId: Int,
    val isVisible: Boolean
)
