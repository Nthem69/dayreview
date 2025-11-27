package com.example.dayreview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dayreview.data.*
import com.example.dayreview.ui.theme.MoodBlue
import com.example.dayreview.ui.theme.MoodOrange
import com.example.dayreview.ui.theme.MoodBlack
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val taskDao = db.taskDao()
    private val habitDao = db.habitDao()
    private val ratingDao = db.ratingDao()

    // --- STATE FLOWS (The UI watches these) ---
    
    // We hold the selected date here so the UI follows it
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // 1. Tasks for the selected date
    val tasks = _selectedDate.flatMapLatest { date ->
        taskDao.getTasksForDate(date.toString())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 2. Ghost Tasks (Unfinished from past)
    val ghostTasks = taskDao.getUnfinishedPastTasks(LocalDate.now().toString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 3. Habits (Always all habits)
    val habits = habitDao.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
        
    // 4. Ratings (All ratings map)
    val ratings = ratingDao.getAllRatings()
        .map { list -> list.associate { LocalDate.parse(it.date) to it.moodId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // --- ACTIONS ---

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            taskDao.insertTask(TaskEntity(title = title, isDone = false, date = _selectedDate.value.toString()))
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isDone = !task.isDone))
        }
    }
    
    fun updateTaskTitle(task: TaskEntity, newTitle: String) {
        viewModelScope.launch {
             taskDao.updateTask(task.copy(title = newTitle))
        }
    }

    fun addHabit(title: String) {
        viewModelScope.launch {
            // Default color BLUE for now
            habitDao.insertHabit(HabitEntity(title = title, colorArgb = android.graphics.Color.parseColor("#4FB3FF")))
        }
    }
    
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = habitDao.getLog(habitId, todayStr)
            if (existing != null) {
                // If exists, toggle or delete? For simplicity, let's toggle boolean
                habitDao.insertLog(existing.copy(isDone = !existing.isDone))
            } else {
                habitDao.insertLog(HabitLogEntity(habitId = habitId, date = todayStr, isDone = true))
            }
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

// Factory to create ViewModel with Application Context
class DayReviewViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DayReviewViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
