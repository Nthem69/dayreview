package com.example.dayreview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// Colors
val MoodBlue = Color(0xFF4FB3FF)
val MoodOrange = Color(0xFFFF6F3B)
val MoodBlack = Color(0xFF1A1A1A)

data class Task(val id: Long, val title: String, val isDone: Boolean)

@Composable
fun TodayScreen() {
    var isDayRated by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // State: List of Tasks
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    // Logic: Real Date
    val today = remember { LocalDate.now() }
    val currentMonth = remember { today.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }, // Opens Dialog
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 20.dp)
        ) {
            // 1. Header & Calendar
            Spacer(modifier = Modifier.height(16.dp))
            TopHeader(monthName = currentMonth)
            Spacer(modifier = Modifier.height(20.dp))
            
            // Pass today to the calendar to highlight the correct day
            MonthCalendar(today = today)
            
            Spacer(modifier = Modifier.height(24.dp))

            // 2. Rating (Vanishing)
            AnimatedVisibility(
                visible = !isDayRated,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Text(
                        text = "How's your day!!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MoodButton(MoodBlue) { isDayRated = true }
                        MoodButton(MoodOrange) { isDayRated = true }
                        MoodButton(MoodBlack) { isDayRated = true }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 3. Task List
            Text(
                text = "Today's Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tasks yet. Tap + to add.", color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // FIX: Space for FAB
                ) {
                    items(tasks.size) { i ->
                        val task = tasks[i]
                        TaskItem(
                            task = task, 
                            onToggle = { 
                                tasks = tasks.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // The Add Task Dialog
    if (showAddDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Task") },
            text = { 
                OutlinedTextField(
                    value = text, 
                    onValueChange = { text = it },
                    placeholder = { Text("What needs doing?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            tasks = tasks + Task(System.currentTimeMillis(), text, false)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun TopHeader(monthName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) { Text("=", fontWeight = FontWeight.Bold) }

        Surface(shape = RoundedCornerShape(50), color = Color(0xFFF5F5F5), modifier = Modifier.height(40.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(monthName, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun MonthCalendar(today: LocalDate) {
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
        
        // Generate days for the current month
        val daysInMonth = today.lengthOfMonth()
        val days = (1..daysInMonth).toList()
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days.size) { index ->
                val dayNum = days[index]
                val isToday = dayNum == today.dayOfMonth
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(
                        when {
                            isToday -> Color.Black
                            else -> Color.Transparent
                        }
                    )
                ) {
                    Text(
                        text = "$dayNum", 
                        fontSize = 12.sp, 
                        color = if (isToday) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun MoodButton(color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color).clickable { onClick() })
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F9FA))
            .clickable { onToggle() }
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
        Text(task.title, color = if (task.isDone) Color.Gray else Color.Black)
    }
}
