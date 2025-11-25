package com.example.dayreview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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

// Define your custom colors from the image
val MoodBlue = Color(0xFF4FB3FF)
val MoodOrange = Color(0xFFFF6F3B)
val MoodBlack = Color(0xFF1A1A1A)
val CalendarGray = Color(0xFFF0F0F0)

@Composable
fun TodayScreen() {
    // State simulating the database
    var isDayRated by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf<Color?>(null) }
    
    // Task State
    data class Task(val id: Int, val title: String, val isDone: Boolean)
    var tasks by remember { mutableStateOf(
        List(10) { Task(it, "Task Item #${it + 1}", false) } // Dummy data to show scrolling
    ) }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add Task Logic later */ },
                containerColor = Color.Black,
                contentColor = Color.White
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
            // --- SECTION 1: TOP BAR & CALENDAR ---
            Spacer(modifier = Modifier.height(16.dp))
            TopHeader()
            Spacer(modifier = Modifier.height(20.dp))
            MonthCalendar()
            
            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 2: RATING (VANISHING) ---
            // This is the magic. It occupies space, but animates away when rated.
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
                        MoodButton(MoodBlue) { isDayRated = true; selectedMood = MoodBlue }
                        MoodButton(MoodOrange) { isDayRated = true; selectedMood = MoodOrange }
                        MoodButton(MoodBlack) { isDayRated = true; selectedMood = MoodBlack }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // --- SECTION 3: TO-DO LIST ---
            // Weight(1f) ensures this fills whatever space is left.
            // If Rating is visible, this is smaller. If Rating vanishes, this grows.
            Text(
                text = "Today's Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Takes remaining space!
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

// --- SUBCOMPONENTS ---

@Composable
fun TopHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu Icon Placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Text("=", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Date Pill
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFF5F5F5),
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("March", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun MonthCalendar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Days of week
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.Gray,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // Dummy Grid (4 weeks)
        // In the real app, this will be calculated from LocalDate
        val days = (1..28).toList()
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(180.dp), // Fixed height for calendar area
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Gap between columns
        ) {
            items(days.size) { index ->
                val dayNum = days[index]
                val isToday = dayNum == 10 // Fake today
                val isPast = dayNum < 10
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isToday -> Color.Black
                                isPast && dayNum % 2 == 0 -> MoodBlue // Fake history
                                isPast -> MoodOrange // Fake history
                                else -> Color.Transparent // Future
                            }
                        )
                ) {
                    Text(
                        text = "$dayNum",
                        fontSize = 12.sp,
                        color = if (isToday || isPast) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun MoodButton(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
    )
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
                .then(if (!task.isDone) Modifier.padding(2.dp).background(Color.White, CircleShape) else Modifier)
                .then(if (!task.isDone) Modifier.border(2.dp, Color.Gray, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (task.isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (task.isDone) Color.Gray else Color.Black,
            // textDecoration = if (task.isDone) TextDecoration.LineThrough else null
        )
    }
}
