package com.example.dayreview.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean,
    val date: String // "YYYY-MM-DD"
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val colorArgb: Int,
    val streak: Int = 0,
    val isDoneToday: Boolean = false
)

// We keep logs for future detailed analytics
@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, 
    val isDone: Boolean
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val date: String, 
    val moodId: String 
)
