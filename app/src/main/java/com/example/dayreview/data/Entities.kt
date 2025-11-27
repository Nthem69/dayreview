package com.example.dayreview.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isDone: Boolean,
    val date: String // Stored as "YYYY-MM-DD"
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val colorArgb: Int
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, // "YYYY-MM-DD"
    val isDone: Boolean
)

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD" (One rating per day)
    val moodId: String // "good", "ok", "bad"
)
