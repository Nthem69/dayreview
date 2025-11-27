package com.example.dayreview.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Get tasks for a specific date
    @Query("SELECT * FROM tasks WHERE date = :date")
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>

    // GHOST TASK LOGIC: Get tasks from yesterday (or older) that are NOT done
    @Query("SELECT * FROM tasks WHERE date < :today AND isDone = 0")
    fun getUnfinishedPastTasks(today: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    // Helper to "Move" a ghost task to today
    @Query("UPDATE tasks SET date = :newDate WHERE id = :taskId")
    suspend fun moveTaskToDate(taskId: Long, newDate: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>
    
    @Insert
    suspend fun insertHabit(habit: HabitEntity)
    
    @Update
    suspend fun updateHabit(habit: HabitEntity)

    // Log a habit execution
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun getLog(habitId: Long, date: String): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    // Heatmap Logic: Get all logs for a habit
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>
}

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings")
    fun getAllRatings(): Flow<List<RatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setRating(rating: RatingEntity)
}
