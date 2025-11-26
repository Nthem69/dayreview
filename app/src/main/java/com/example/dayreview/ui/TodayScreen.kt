package com.example.dayreview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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

@Composable
fun TodayScreen() {
    // --- STATE ---
    
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    
    // Rating State
    var ratedDays by remember { mutableStateOf(mapOf<LocalDate, MoodOption>()) }

    // Task State (Simulating a DB with a list)
    // Seed some dummy data for previous days to test "Read Only" view
    var allTasks by remember { mutableStateOf(
        listOf(
            Task(1, "Water Plants", false, today.minusDays(2)), // Missed task on Friday?
            Task(2, "Buy Groceries", true, today.minusDays(1)),
            Task(3, "Setup Project", false, today)
        )
    ) }
    
    // Derived State
    val visibleTasks = allTasks.filter { it.date == selectedDate }
    val isToday = selectedDate == today
    val isFuture = selectedDate.isAfter(today)
    val isEditable = isToday || isFuture // Can edit today and future, NOT past.

    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            // FAB only visible if we can edit this day
            if (isEditable) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, contentDescription = "Add") }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center everything by default
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- TOP HEADER ---
            TopHeader(
                currentDate = selectedDate,
                onMonthSelected = { newMonth -> 
                    selectedDate = selectedDate.withMonth(newMonth.value).withDayOfMonth(1)
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // --- REAL CALENDAR ---
            MonthCalendar(
                displayedDate = selectedDate,
                today = today,
                ratedDays = ratedDays,
                onDateSelected = { selectedDate = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // --- RATING SECTION (Strictly Today Only) ---
            val isRated = ratedDays.containsKey(today)
            // Show only if viewing TODAY and NOT rated yet
            AnimatedVisibility(
                visible = isToday && !isRated,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "How's your day!!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AvailableMoods.forEach { mood ->
                            MoodButton(mood) { 
                                ratedDays = ratedDays + (today to mood) 
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // --- TASK LIST ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(
                    text = if (isToday) "Today's Plan" else "Plan for ${selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            if (visibleTasks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        if (isEditable) "No tasks. Tap + to add." else "No tasks recorded.", 
                        color = Color.LightGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(visibleTasks.size) { i ->
                        val task = visibleTasks[i]
                        TaskItem(
                            task = task,
                            enabled = isEditable, // Pass editable state down
                            onToggle = { 
                                // Only update if editable
                                if (isEditable) {
                                    allTasks = allTasks.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
                                }
                            },
                            onLongClick = {
                                if (isEditable) taskToEdit = task
                            }
                        )
                    }
                }
            }
        }
    }
    
    // --- DIALOGS ---
    
    if (showAddDialog) {
        TaskDialog(
            title = "New Task for $selectedDate",
            initialText = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { text ->
                allTasks = allTasks + Task(System.currentTimeMillis(), text, false, selectedDate)
                showAddDialog = false
            }
        )
    }
    
    if (taskToEdit != null) {
        TaskDialog(
            title = "Edit Task",
            initialText = taskToEdit!!.title,
            onDismiss = { taskToEdit = null },
            onConfirm = { text ->
                allTasks = allTasks.map { if (it.id == taskToEdit!!.id) it.copy(title = text) else it }
                taskToEdit = null
            }
        )
    }
}

// --- SUBCOMPONENTS ---

@Composable
fun TopHeader(currentDate: LocalDate, onMonthSelected: (Month) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    val monthName = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) { Text("=", fontWeight = FontWeight.Bold) }

        Box {
            Surface(
                shape = RoundedCornerShape(50), 
                color = Color(0xFFF5F5F5), 
                modifier = Modifier.height(40.dp).clickable { menuExpanded = true }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(monthName, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(Color.White)) {
                Month.values().forEach { month ->
                    DropdownMenuItem(text = { Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault())) }, onClick = { onMonthSelected(month); menuExpanded = false })
                }
            }
        }
    }
}

@Composable
fun MonthCalendar(
    displayedDate: LocalDate, 
    today: LocalDate,
    ratedDays: Map<LocalDate, MoodOption>,
    onDateSelected: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // --- REAL CALENDAR LOGIC ---
        val daysInMonth = displayedDate.lengthOfMonth()
        val firstDayOfMonth = displayedDate.withDayOfMonth(1)
        // Adjust offset: Java Time Sun=7, Mon=1. Grid is S(0), M(1)...
        // If 1st is Sun(7), offset=0. If Mon(1), offset=1.
        val startOffset = firstDayOfMonth.dayOfWeek.value % 7 
        
        val totalCells = daysInMonth + startOffset
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(220.dp), // Slightly taller for 6 rows
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(totalCells) { index ->
                if (index < startOffset) {
                    // Empty Cell (Previous Month)
                    Box(modifier = Modifier.size(32.dp))
                } else {
                    val dayNum = index - startOffset + 1
                    val cellDate = displayedDate.withDayOfMonth(dayNum)
                    
                    val isToday = cellDate == today
                    val isSelected = cellDate == displayedDate
                    val rating = ratedDays[cellDate]
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(rating?.shape ?: CircleShape)
                            .background(
                                when {
                                    rating != null -> rating.color
                                    isSelected -> Color.Black 
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onDateSelected(cellDate) }
                    ) {
                        Text(
                            text = "$dayNum", 
                            fontSize = 12.sp, 
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (rating != null || isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodButton(mood: MoodOption, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).clip(mood.shape).background(mood.color).clickable { onClick() }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(task: Task, enabled: Boolean, onToggle: () -> Unit, onLongClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F9FA))
            .combinedClickable(
                enabled = true, // We always allow click, but we gate logic inside
                onClick = { if (enabled) onToggle() },
                onLongClick = { if (enabled) onLongClick() }
            )
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (task.isDone) Color.Black else Color.Transparent, CircleShape)
                .border(2.dp, if(task.isDone) Color.Black else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (task.isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            task.title, 
            color = if (task.isDone) Color.Gray else Color.Black,
            style = if (task.isDone) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun TaskDialog(title: String, initialText: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { 
            OutlinedTextField(
                value = text, 
                onValueChange = { text = it },
                placeholder = { Text("Task description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            ) 
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        },
        containerColor = Color.White
    )
}
