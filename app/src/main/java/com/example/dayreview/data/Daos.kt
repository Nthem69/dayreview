package com.example.dayreview.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date") fun getTasksForDate(date: String): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks WHERE date < :today AND isDone = 0") fun getUnfinishedPastTasks(today: String): Flow<List<TaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTask(task: TaskEntity)
    @Update suspend fun updateTask(task: TaskEntity)
    @Delete suspend fun deleteTask(task: TaskEntity)
}

@Dao interface HabitDao {
    @Query("SELECT * FROM habits") fun getAllHabits(): Flow<List<HabitEntity>>
    @Insert suspend fun insertHabit(habit: HabitEntity)
    @Update suspend fun updateHabit(habit: HabitEntity)
    @Query("DELETE FROM habits WHERE id = :id") suspend fun deleteHabitById(id: Long)
}

@Dao interface RatingDao {
    @Query("SELECT * FROM ratings") fun getAllRatings(): Flow<List<RatingEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun setRating(rating: RatingEntity)
}

@Dao interface MoodConfigDao {
    @Query("SELECT * FROM mood_config ORDER BY id ASC") fun getAllConfigs(): Flow<List<MoodConfigEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertConfig(config: MoodConfigEntity)
    @Update suspend fun updateConfig(config: MoodConfigEntity)
}
