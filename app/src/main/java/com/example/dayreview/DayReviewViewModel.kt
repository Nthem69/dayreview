package com.example.dayreview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dayreview.R
import com.example.dayreview.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max
import kotlin.random.Random

class DayReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val taskDao = db.taskDao()
    private val habitDao = db.habitDao()
    private val ratingDao = db.ratingDao()
    private val moodDao = db.moodConfigDao()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _revealedItemId = MutableStateFlow<String?>(null)
    val revealedItemId = _revealedItemId.asStateFlow()

    val tasks = _selectedDate.flatMapLatest { date -> taskDao.getTasksForDate(date.toString()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val ghostTasks = taskDao.getUnfinishedPastTasks(LocalDate.now().toString()).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val habits = habitDao.getAllHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val ratings = ratingDao.getAllRatings().map { list -> list.associate { LocalDate.parse(it.date) to it.moodId } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())
    val moodConfigs = moodDao.getAllConfigs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        viewModelScope.launch {
            if (moodDao.getAllConfigs().first().isEmpty()) {
                moodDao.insertConfig(MoodConfigEntity(1, "Awful", android.graphics.Color.parseColor("#9C27B0"), R.drawable.ic_mood_1, true))
                moodDao.insertConfig(MoodConfigEntity(2, "Bad", android.graphics.Color.parseColor("#F44336"), R.drawable.ic_mood_2, true))
                moodDao.insertConfig(MoodConfigEntity(3, "Okay", android.graphics.Color.parseColor("#FFC107"), R.drawable.ic_mood_3, true))
                moodDao.insertConfig(MoodConfigEntity(4, "Good", android.graphics.Color.parseColor("#76FF03"), R.drawable.ic_mood_4, true))
                moodDao.insertConfig(MoodConfigEntity(5, "???", android.graphics.Color.parseColor("#9E9E9E"), R.drawable.ic_mood_5, true))
            }
        }
    }
    
    fun setRevealedItem(id: String?) { _revealedItemId.value = id }
    fun setDate(date: LocalDate) { _selectedDate.value = date }
    
    fun setYearMonth(ym: YearMonth) {
        val today = LocalDate.now()
        if (ym.year == today.year && ym.month == today.month) setDate(today) 
        else setDate(today.withYear(ym.year).withMonth(ym.monthValue).withDayOfMonth(1))
    }
    fun changeMonth(newMonthValue: Int) {
        val today = LocalDate.now()
        val currentSelected = _selectedDate.value
        if (newMonthValue == today.monthValue && currentSelected.year == today.year) setDate(today) 
        else setDate(currentSelected.withMonth(newMonthValue).withDayOfMonth(1))
    }

    fun addTask(title: String, time: String?) { viewModelScope.launch { taskDao.insertTask(TaskEntity(title = title, isDone = false, date = _selectedDate.value.toString(), time = time)) } }
    fun toggleTask(task: TaskEntity) { viewModelScope.launch { taskDao.updateTask(task.copy(isDone = !task.isDone)) } }
    fun updateTaskDetails(task: TaskEntity, newTitle: String, newTime: String?) { viewModelScope.launch { taskDao.updateTask(task.copy(title = newTitle, time = newTime)) } }
    fun deleteTask(task: TaskEntity) { viewModelScope.launch { taskDao.deleteTask(task) } }

    fun addHabit(title: String, color: Int) { 
        val history = List(30) { false } 
        viewModelScope.launch { habitDao.insertHabit(HabitEntity(title = title, colorArgb = color, history = history)) } 
    }
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = habits.value.find { it.id == habitId } ?: return@launch
            val newStatus = !habit.isDoneToday
            val todayIdx = LocalDate.now().dayOfMonth - 1
            val newHistory = habit.history.toMutableList()
            if (todayIdx in newHistory.indices) newHistory[todayIdx] = newStatus
            val newStreak = if (newStatus) habit.streak + 1 else max(0, habit.streak - 1)
            habitDao.updateHabit(habit.copy(isDoneToday = newStatus, history = newHistory, streak = newStreak))
        }
    }
    fun updateHabit(habit: HabitEntity) { viewModelScope.launch { habitDao.updateHabit(habit) } }
    fun deleteHabit(habit: HabitEntity) { viewModelScope.launch { habitDao.deleteHabitById(habit.id) } }
    fun setRating(moodId: Int) { viewModelScope.launch { ratingDao.setRating(RatingEntity(date = LocalDate.now().toString(), moodId = moodId)) } }
    fun updateMoodConfig(config: MoodConfigEntity) { viewModelScope.launch { moodDao.updateConfig(config) } }
}

class DayReviewViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayReviewViewModel::class.java)) return DayReviewViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
