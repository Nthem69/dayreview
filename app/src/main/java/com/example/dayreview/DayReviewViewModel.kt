package com.example.dayreview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dayreview.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val taskDao = db.taskDao()
    private val habitDao = db.habitDao()
    private val ratingDao = db.ratingDao()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    val tasks = _selectedDate.flatMapLatest { date ->
        taskDao.getTasksForDate(date.toString())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val ghostTasks = taskDao.getUnfinishedPastTasks(LocalDate.now().toString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val habits = habitDao.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
        
    val ratings = ratingDao.getAllRatings()
        .map { list -> list.associate { LocalDate.parse(it.date) to it.moodId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    fun setDate(date: LocalDate) { _selectedDate.value = date }

    fun addTask(title: String) {
        viewModelScope.launch {
            taskDao.insertTask(TaskEntity(title = title, isDone = false, date = _selectedDate.value.toString()))
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch { taskDao.updateTask(task.copy(isDone = !task.isDone)) }
    }
    
    fun updateTaskTitle(task: TaskEntity, newTitle: String) {
        viewModelScope.launch { taskDao.updateTask(task.copy(title = newTitle)) }
    }

    fun addHabit(title: String) {
        viewModelScope.launch {
            // Initialize with empty history of 30 days false
            val initialHistory = List(30) { false }
            habitDao.insertHabit(HabitEntity(title = title, colorArgb = android.graphics.Color.parseColor("#4FB3FF"), history = initialHistory))
        }
    }
    
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            // Find current habit, toggle isDoneToday, and update history index for today
            val currentList = habits.value
            val habit = currentList.find { it.id == habitId } ?: return@launch
            
            val newStatus = !habit.isDoneToday
            val todayDayIndex = LocalDate.now().dayOfMonth - 1
            
            // Update history list safely
            val newHistory = habit.history.toMutableList()
            if (todayDayIndex >= 0 && todayDayIndex < newHistory.size) {
                newHistory[todayDayIndex] = newStatus
            }
            
            habitDao.updateHabit(habit.copy(isDoneToday = newStatus, history = newHistory, streak = if(newStatus) habit.streak + 1 else habit.streak))
        }
    }
    
    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch { habitDao.updateHabit(habit) }
    }

    fun setRating(moodId: String) {
        viewModelScope.launch {
            ratingDao.setRating(RatingEntity(date = LocalDate.now().toString(), moodId = moodId))
        }
    }
}
