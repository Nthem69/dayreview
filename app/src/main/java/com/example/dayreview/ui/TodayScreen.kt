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

// Define colors
val MoodBlue = Color(0xFF4FB3FF)
val MoodOrange = Color(0xFFFF6F3B)
val MoodBlack = Color(0xFF1A1A1A)

// --- MOVED UP HERE (Fixes the build error) ---
data class Task(val id: Int, val title: String, val isDone: Boolean)

@Composable
fun TodayScreen() {
    var isDayRated by remember { mutableStateOf(false) }
    
    // Simulating tasks
    var tasks by remember { mutableStateOf(
        List(10) { Task(it, "Task Item #${it + 1}", false) }
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
            // 1. Header & Calendar
            Spacer(modifier = Modifier.height(16.dp))
            TopHeader()
            Spacer(modifier = Modifier.height(20.dp))
            MonthCalendar()
            
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
            
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
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

@Composable
fun TopHeader() {
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
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        val days = (1..28).toList()
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days.size) { index ->
                val dayNum = days[index]
                val isToday = dayNum == 10
                val isPast = dayNum < 10
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(
                        when {
                            isToday -> Color.Black
                            isPast && dayNum % 2 == 0 -> MoodBlue
                            isPast -> MoodOrange
                            else -> Color.Transparent
                        }
                    )
                ) {
                    Text("$dayNum", fontSize = 12.sp, color = if (isToday || isPast) Color.White else Color.Black)
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
                .border(2.dp, Color.Gray, CircleShape),
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
