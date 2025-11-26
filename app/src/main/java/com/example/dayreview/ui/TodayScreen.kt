package com.example.dayreview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

// Import Colors
import com.example.dayreview.ui.theme.MoodBlue
import com.example.dayreview.ui.theme.MoodOrange
import com.example.dayreview.ui.theme.MoodBlack

// --- DATA MODELS ---

data class Task(val id: Long, val title: String, val isDone: Boolean, val date: LocalDate)
// Added 'color' to Habit for customization
data class Habit(val id: Long, val title: String, val isDone: Boolean, val streak: Int, val history: List<Boolean>, val color: Color)
data class MoodOption(val id: String, val color: Color, val shape: Shape)

// Custom Shapes
val TriangleShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
}

val AvailableMoods = listOf(
    MoodOption("good", MoodBlue, CircleShape),
    MoodOption("ok", MoodOrange, RoundedCornerShape(8.dp)),
    MoodOption("bad", MoodBlack, TriangleShape)
)

// Habit Colors for Picker
val HabitColors = listOf(MoodBlue, MoodOrange, Color(0xFF4CAF50), Color(0xFFE91E63), Color(0xFF9C27B0))

enum class AppTab { Plan, Habits, Tracker }

@Composable
fun TodayScreen() {
    val today = remember { LocalDate.now() }
    val currentTime = remember { LocalTime.now() }
    
    var selectedDate by remember { mutableStateOf(today) }
    var currentTab by remember { mutableStateOf(AppTab.Plan) }
    var ratedDays by remember { mutableStateOf(mapOf<LocalDate, MoodOption>()) }

    // --- TASK STATE ---
    var allTasks by remember { mutableStateOf(
        listOf(
            Task(1, "Setup Project", true, today),
            Task(2, "Water Plants", false, today)
        )
    ) }
    
    // --- HABIT STATE ---
    fun generateHistory(days: Int, isDoneToday: Boolean): List<Boolean> {
        return List(days) { index -> if (index == today.dayOfMonth - 1) isDoneToday else (index % 3 != 0) }
    }
    val daysInMonth = today.lengthOfMonth()
    
    var habits by remember { mutableStateOf(
        listOf(
            Habit(1, "💻 Build in Public", true, 24, generateHistory(daysInMonth, true), MoodBlue),
            Habit(2, "📚 Read 10 pages", false, 5, generateHistory(daysInMonth, false), MoodBlue),
            Habit(3, "💪 Workout", true, 3, generateHistory(daysInMonth, true), MoodBlue)
        )
    ) }

    val isToday = selectedDate == today
    val isEditable = selectedDate >= today
    
    // Dialog States
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            // UNIVERSAL FAB LOGIC
            val onClickAction: () -> Unit = when (currentTab) {
                AppTab.Plan -> { { showAddTaskDialog = true } }
                AppTab.Habits -> { { showAddHabitDialog = true } }
                AppTab.Tracker -> { { /* Tracker Add */ } }
            }
            
            // Show FAB if editable or on Habits/Tracker
            if (isEditable || currentTab != AppTab.Plan) {
                FloatingActionButton(onClick = onClickAction, containerColor = Color.Black, contentColor = Color.White, shape = CircleShape) { 
                    Icon(Icons.Default.Add, "Add") 
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TopHeader(selectedDate) { newMonth -> selectedDate = selectedDate.withMonth(newMonth.value).withDayOfMonth(1) }
            Spacer(modifier = Modifier.height(12.dp))
            
            // 1. COMPACT CALENDAR (Removed AspectRatio, using tighter height)
            MonthCalendar(selectedDate, today, ratedDays) { selectedDate = it }
            Spacer(modifier = Modifier.height(12.dp))

            // 2. TIME-GATED RATING (After 2 PM)
            // Logic: Is today + Not Rated + Time >= 14:00 (2 PM)
            val isRated = ratedDays.containsKey(today)
            val isTimeToShow = currentTime.hour >= 14 // 2 PM threshold
            
            AnimatedVisibility(visible = isToday && !isRated && isTimeToShow) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("How's your day!!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AvailableMoods.forEach { mood -> MoodButton(mood) { ratedDays = ratedDays + (today to mood) } }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            TabSegmentControl(currentTab) { currentTab = it }
            Spacer(modifier = Modifier.height(12.dp))

            Crossfade(targetState = currentTab, label = "Tab") { tab ->
                when (tab) {
                    AppTab.Plan -> PlanContent(
                        tasks = allTasks.filter { it.date == selectedDate },
                        isEditable = isEditable,
                        onCheck = { id -> allTasks = allTasks.map { if (it.id == id && !it.isDone) it.copy(isDone = true) else it } },
                        onUncheck = { id -> allTasks = allTasks.map { if (it.id == id && it.isDone) it.copy(isDone = false) else it } },
                        onEdit = { task -> if (isEditable) taskToEdit = task }
                    )
                    AppTab.Habits -> HabitsContent(
                        habits = habits,
                        daysInMonth = daysInMonth,
                        onToggle = { id -> 
                            habits = habits.map { h -> 
                                if (h.id == id) {
                                    val newStatus = !h.isDone
                                    val newHistory = h.history.toMutableList().apply { 
                                        if (today.dayOfMonth - 1 in indices) this[today.dayOfMonth - 1] = newStatus 
                                    }
                                    h.copy(isDone = newStatus, history = newHistory)
                                } else h 
                            } 
                        },
                        onEdit = { habitToEdit = it }
                    )
                    AppTab.Tracker -> TrackerContent()
                }
            }
        }
    }
    
    // Dialogs
    if (showAddTaskDialog) { TaskDialog("New Task", "", { showAddTaskDialog = false }) { txt -> allTasks = allTasks + Task(System.currentTimeMillis(), txt, false, selectedDate); showAddTaskDialog = false } }
    if (showAddHabitDialog) { TaskDialog("New Habit", "", { showAddHabitDialog = false }) { txt -> habits = habits + Habit(System.currentTimeMillis(), txt, false, 0, generateHistory(daysInMonth, false), MoodBlue); showAddHabitDialog = false } }
    
    if (taskToEdit != null) { TaskDialog("Edit Task", taskToEdit!!.title, { taskToEdit = null }) { txt -> allTasks = allTasks.map { if (it.id == taskToEdit!!.id) it.copy(title = txt) else it }; taskToEdit = null } }
    
    if (habitToEdit != null) { 
        HabitEditDialog(
            habit = habitToEdit!!,
            onDismiss = { habitToEdit = null },
            onConfirm = { title, color -> 
                habits = habits.map { if (it.id == habitToEdit!!.id) it.copy(title = title, color = color) else it }
                habitToEdit = null
            }
        )
    }
}

// --- CALENDAR OPTIMIZATION ---
@Composable
fun MonthCalendar(displayedDate: LocalDate, today: LocalDate, ratedDays: Map<LocalDate, MoodOption>, onDateSelected: (LocalDate) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // REMOVED AspectRatio(1f). Using a compact fixed height based on visual needs.
            // 260dp fits 6 rows tightly without wasting vertical space.
            .height(260.dp) 
            .background(Color(0xFFF8F9FA), RoundedCornerShape(24.dp))
            .padding(12.dp) // Reduced padding
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day -> Text(day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        
        val daysInMonth = displayedDate.lengthOfMonth()
        val startOffset = displayedDate.withDayOfMonth(1).dayOfWeek.value % 7 
        val totalCells = 42
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalArrangement = Arrangement.SpaceEvenly,
            userScrollEnabled = false
        ) {
            items(totalCells) { index ->
                val dayNum = index - startOffset + 1
                if (index < startOffset || dayNum > daysInMonth) {
                    Box(modifier = Modifier.size(30.dp)) // Ghost cell
                } else {
                    val cellDate = displayedDate.withDayOfMonth(dayNum)
                    val rating = ratedDays[cellDate]; val isSelected = cellDate == displayedDate; val isToday = cellDate == today
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp) // Fixed small size
                            .clip(rating?.shape ?: CircleShape)
                            .background(when { rating != null -> rating.color; isSelected -> Color.Black; else -> Color.Transparent })
                            .clickable { onDateSelected(cellDate) }
                    ) {
                        Text("$dayNum", fontSize = 12.sp, fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium, color = if (rating != null || isSelected) Color.White else Color.Black)
                    }
                }
            }
        }
    }
}

// --- HABIT UI RESTRUCTURE ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsContent(habits: List<Habit>, daysInMonth: Int, onToggle: (Long) -> Unit, onEdit: (Habit) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        items(habits.size) { i ->
            val habit = habits[i]
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = { /* Do nothing on card click, click check to toggle */ },
                        onLongClick = { onEdit(habit) } // Long press card to edit
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Icon & Details (Left side)
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Heatmap
                    val rows = 3
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(rows) { r ->
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(10) { c ->
                                    val dayIndex = r * 10 + c
                                    if (dayIndex < daysInMonth) {
                                        val isFilled = if (dayIndex < habit.history.size) habit.history[dayIndex] else false
                                        Box(
                                            modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp))
                                                .background(if (isFilled) habit.color else Color(0xFFF0F0F0)) // Use habit custom color
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 2. Stats & Action (Right side)
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Streak (Top Right)
                    Text("🔥 ${habit.streak}", fontSize = 12.sp, color = Color.Gray)
                    
                    // Check Button (Bottom Right)
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(if (habit.isDone) habit.color else Color(0xFFF0F0F0))
                            .clickable { onToggle(habit.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (habit.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HabitEditDialog(habit: Habit, onDismiss: () -> Unit, onConfirm: (String, Color) -> Unit) {
    var text by remember { mutableStateOf(habit.title) }
    var selectedColor by remember { mutableStateOf(habit.color) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Habit") },
        text = { 
            Column {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Title") }, singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Heatmap Color", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitColors.forEach { color ->
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(color)
                                .border(2.dp, if (selectedColor == color) Color.Black else Color.Transparent, CircleShape)
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text, selectedColor) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } },
        containerColor = Color.White
    )
}

// ... (Rest of existing helpers: PlanContent, TabSegmentControl, TrackerContent, TopHeader, MoodButton, TaskDialog)
@Composable
fun TabSegmentControl(selected: AppTab, onSelect: (AppTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        AppTab.values().forEach { tab ->
            val isSelected = selected == tab
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White else Color.Transparent).clickable { onSelect(tab) }) {
                Text(tab.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.Black else Color.Gray, fontSize = 14.sp)
            }
        }
    }
}
@Composable
fun TrackerContent() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tracker coming soon...", color = Color.Gray) } }
@Composable
fun TopHeader(currentDate: LocalDate, onMonthSelected: (Month) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) { Text("=", fontWeight = FontWeight.Bold) }
        Box {
            Surface(shape = RoundedCornerShape(50), color = Color(0xFFF5F5F5), modifier = Modifier.height(40.dp).clickable { menuExpanded = true }) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(Color.White)) {
                Month.values().forEach { month -> DropdownMenuItem(text = { Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault())) }, onClick = { onMonthSelected(month); menuExpanded = false }) }
            }
        }
    }
}
@Composable
fun MoodButton(mood: MoodOption, onClick: () -> Unit) { Box(modifier = Modifier.size(48.dp).clip(mood.shape).background(mood.color).clickable { onClick() }) }
@Composable
fun TaskDialog(title: String, initialText: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }, confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }, containerColor = Color.White)
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanContent(tasks: List<Task>, isEditable: Boolean, onCheck: (Long) -> Unit, onUncheck: (Long) -> Unit, onEdit: (Task) -> Unit) {
    if (tasks.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No tasks today.", color = Color.LightGray) } }
    else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(tasks.size, key = { tasks[it].id }) { i ->
                val task = tasks[i]
                val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onEdit(task); false } else false })
                SwipeToDismissBox(state = dismissState, backgroundContent = { Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.LightGray).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) } }, enableDismissFromStartToEnd = false) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF8F9FA)).combinedClickable(onClick = { onCheck(task.id) }, onLongClick = { onUncheck(task.id) }).padding(16.dp)) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (task.isDone) Color.Black else Color.Transparent, CircleShape).border(2.dp, if(task.isDone) Color.Black else Color.Gray, CircleShape), contentAlignment = Alignment.Center) { if (task.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(task.title, color = if (task.isDone) Color.Gray else Color.Black, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
