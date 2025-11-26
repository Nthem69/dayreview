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
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

// Import Colors
import com.example.dayreview.ui.theme.MoodBlue
import com.example.dayreview.ui.theme.MoodOrange
import com.example.dayreview.ui.theme.MoodBlack

// --- DATA MODELS ---

data class Task(val id: Long, val title: String, val isDone: Boolean, val date: LocalDate)
// Updated Habit: Title contains emoji, history tracks the month's progress
data class Habit(val id: Long, val title: String, val isDone: Boolean, val streak: Int, val history: List<Boolean>)
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

enum class AppTab { Plan, Habits, Tracker }

@Composable
fun TodayScreen() {
    val today = remember { LocalDate.now() }
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
    // Dummy generator: Creates a history list where previous days are randomly done, today is linked to isDone
    fun generateHistory(days: Int, isDoneToday: Boolean): List<Boolean> {
        return List(days) { index -> 
            if (index == today.dayOfMonth - 1) isDoneToday else (index % 3 != 0) // Random pattern
        }
    }
    
    val daysInMonth = today.lengthOfMonth()
    var habits by remember { mutableStateOf(
        listOf(
            Habit(1, "💻 Build in Public", true, 24, generateHistory(daysInMonth, true)),
            Habit(2, "📚 Read 10 pages", false, 5, generateHistory(daysInMonth, false)),
            Habit(3, "💪 Workout", true, 3, generateHistory(daysInMonth, true))
        )
    ) }

    val isToday = selectedDate == today
    val isEditable = selectedDate >= today
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (currentTab == AppTab.Plan && isEditable) {
                FloatingActionButton(onClick = { showAddTaskDialog = true }, containerColor = Color.Black, contentColor = Color.White, shape = CircleShape) { Icon(Icons.Default.Add, "Add") }
            } else if (currentTab == AppTab.Habits) {
                 FloatingActionButton(onClick = { /* Add Habit */ }, containerColor = Color.Black, contentColor = Color.White, shape = CircleShape) { Icon(Icons.Default.Add, "Add") }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TopHeader(selectedDate) { newMonth -> selectedDate = selectedDate.withMonth(newMonth.value).withDayOfMonth(1) }
            Spacer(modifier = Modifier.height(20.dp))
            
            // 1. Calendar Fix: Use AspectRatio(1f) to ensure squareness that fits ALL rows
            MonthCalendar(selectedDate, today, ratedDays) { selectedDate = it }
            Spacer(modifier = Modifier.height(12.dp))

            val isRated = ratedDays.containsKey(today)
            AnimatedVisibility(visible = isToday && !isRated) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("How's your day!!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AvailableMoods.forEach { mood -> MoodButton(mood) { ratedDays = ratedDays + (today to mood) } }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            TabSegmentControl(currentTab) { currentTab = it }
            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(targetState = currentTab, label = "Tab") { tab ->
                when (tab) {
                    AppTab.Plan -> PlanContent(
                        tasks = allTasks.filter { it.date == selectedDate },
                        isEditable = isEditable,
                        onCheck = { id -> 
                            // Only allow checking if NOT done (Tap to Check)
                            allTasks = allTasks.map { if (it.id == id && !it.isDone) it.copy(isDone = true) else it }
                        },
                        onUncheck = { id ->
                            // Long press to Uncheck
                            allTasks = allTasks.map { if (it.id == id && it.isDone) it.copy(isDone = false) else it }
                        },
                        onEdit = { task -> if (isEditable) taskToEdit = task }
                    )
                    AppTab.Habits -> HabitsContent(
                        habits = habits,
                        daysInMonth = daysInMonth,
                        onToggle = { id -> 
                            habits = habits.map { h -> 
                                if (h.id == id) {
                                    val newStatus = !h.isDone
                                    // Update history for today (index = dayOfMonth - 1)
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
    if (taskToEdit != null) { TaskDialog("Edit Task", taskToEdit!!.title, { taskToEdit = null }) { txt -> allTasks = allTasks.map { if (it.id == taskToEdit!!.id) it.copy(title = txt) else it }; taskToEdit = null } }
    if (habitToEdit != null) { TaskDialog("Edit Habit", habitToEdit!!.title, { habitToEdit = null }) { txt -> habits = habits.map { if (it.id == habitToEdit!!.id) it.copy(title = txt) else it }; habitToEdit = null } }
}

// --- CALENDAR FIX ---
@Composable
fun MonthCalendar(displayedDate: LocalDate, today: LocalDate, ratedDays: Map<LocalDate, MoodOption>, onDateSelected: (LocalDate) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // FORCE SQUARE SHAPE
            .background(Color(0xFFF8F9FA), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day -> Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        val daysInMonth = displayedDate.lengthOfMonth()
        val startOffset = displayedDate.withDayOfMonth(1).dayOfWeek.value % 7 
        val totalCells = 42 // Fixed 6 rows
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(), // Fill the square container
            verticalArrangement = Arrangement.SpaceEvenly, // Distribute evenly
            horizontalArrangement = Arrangement.SpaceEvenly,
            userScrollEnabled = false
        ) {
            items(totalCells) { index ->
                val dayNum = index - startOffset + 1
                if (index < startOffset || dayNum > daysInMonth) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val cellDate = displayedDate.withDayOfMonth(dayNum)
                    val rating = ratedDays[cellDate]; val isSelected = cellDate == displayedDate; val isToday = cellDate == today
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.aspectRatio(1f).clip(rating?.shape ?: CircleShape)
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

// --- TASK CONTENT (Swipe to Edit, Tap/Hold Check) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanContent(tasks: List<Task>, isEditable: Boolean, onCheck: (Long) -> Unit, onUncheck: (Long) -> Unit, onEdit: (Task) -> Unit) {
    if (tasks.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No tasks today.", color = Color.LightGray) } }
    else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(tasks.size, key = { tasks[it].id }) { i ->
                val task = tasks[i]
                
                // Swipe to Edit Box
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) { onEdit(task); false } else false
                    }
                )
                
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.LightGray).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF8F9FA))
                            .combinedClickable(
                                onClick = { onCheck(task.id) },
                                onLongClick = { onUncheck(task.id) }
                            )
                            .padding(16.dp)
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (task.isDone) Color.Black else Color.Transparent, CircleShape).border(2.dp, if(task.isDone) Color.Black else Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                            if (task.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(task.title, color = if (task.isDone) Color.Gray else Color.Black, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

// --- HABITS CONTENT (Heatmap = Month Days) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsContent(habits: List<Habit>, daysInMonth: Int, onToggle: (Long) -> Unit, onEdit: (Habit) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        items(habits.size) { i ->
            val habit = habits[i]
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = { onToggle(habit.id) },
                        onLongClick = { onEdit(habit) }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.Top // Align top for title vs check
            ) {
                // Main Content
                Column(modifier = Modifier.weight(1f)) {
                    // Title Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(habit.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("�� ${habit.streak}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Heatmap Grid (Dynamic based on daysInMonth)
                    // We render them in rows of ~15 to fit width
                    val rows = 3
                    val cols = (daysInMonth + rows - 1) / rows 
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(rows) { r ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(10) { c -> // Fixed 10 cols for layout stability
                                    val dayIndex = r * 10 + c
                                    if (dayIndex < daysInMonth) {
                                        val isFilled = if (dayIndex < habit.history.size) habit.history[dayIndex] else false
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp) // Small squares
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (isFilled) MoodBlue else Color(0xFFF0F0F0))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                
                // Check Button (Aligned Top-Right ish)
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(if (habit.isDone) MoodBlue else Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (habit.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ... (Rest of existing helpers: TabSegmentControl, TrackerContent, TopHeader, MoodButton, TaskDialog)
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
