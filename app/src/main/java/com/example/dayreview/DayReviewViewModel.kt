package com.example.dayreview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dayreview.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.max
import kotlin.random.Random

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

    init {
        viewModelScope.launch {
            if (habitDao.getAllHabits().first().isEmpty()) {
                // RANDOM HISTORY GENERATOR for initial dummy data
                fun randomHistory(): List<Boolean> = List(30) { Random.nextBoolean() }
                
                habitDao.insertHabit(HabitEntity(title = "💻 Build in Public", colorArgb = android.graphics.Color.parseColor("#4FB3FF"), history = randomHistory(), streak = 6))
                habitDao.insertHabit(HabitEntity(title = "📚 Read 10 pages", colorArgb = android.graphics.Color.parseColor("#FF6F3B"), history = randomHistory(), streak = 1))
                habitDao.insertHabit(HabitEntity(title = "💪 Workout", colorArgb = android.graphics.Color.parseColor("#E0E0E0"), history = randomHistory(), streak = 0))
                addTask("Setup Project")
            }
        }
    }

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
            val initialHistory = List(30) { false }
            habitDao.insertHabit(HabitEntity(title = title, colorArgb = android.graphics.Color.parseColor("#4FB3FF"), history = initialHistory))
        }
    }
    
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val currentList = habits.value
            val habit = currentList.find { it.id == habitId } ?: return@launch
            
            val newStatus = !habit.isDoneToday
            val todayDayIndex = LocalDate.now().dayOfMonth - 1
            
            val newHistory = habit.history.toMutableList()
            if (todayDayIndex >= 0 && todayDayIndex < newHistory.size) {
                newHistory[todayDayIndex] = newStatus
            }
            
            val newStreak = if (newStatus) habit.streak + 1 else max(0, habit.streak - 1)
            
            habitDao.updateHabit(habit.copy(isDoneToday = newStatus, history = newHistory, streak = newStreak))
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

class DayReviewViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DayReviewViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
