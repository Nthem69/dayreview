package com.example.dayreview.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// REQUIRED for Material3 TopAppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen() {
    // State logic
    var dayOffset by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    
    // Simple in-memory task list (reset on restart)
    data class Task(val id: Long, val title: String, val dayOffset: Int, val done: Boolean = false)
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    val todaysTasks = tasks.filter { it.dayOffset == dayOffset }
    
    val title = when (dayOffset) {
        0 -> "Today"
        1 -> "Tomorrow"
        -1 -> "Yesterday"
        else -> if (dayOffset > 0) "In $dayOffset days" else "${-dayOffset} days ago"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DayReview — $title") },
                actions = {
                    TextButton(onClick = { dayOffset -= 1 }) { Text("Prev") }
                    TextButton(onClick = { dayOffset += 1 }) { Text("Next") }
                }
            )
        },
        floatingActionButton = { 
            FloatingActionButton(onClick = { showAdd = true }) { Text("+") } 
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            if (todaysTasks.isEmpty()) {
                Text("No tasks yet.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text("Tap + to add one.", style = MaterialTheme.typography.bodySmall)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(todaysTasks, key = { it.id }) { t ->
                        ElevatedCard {
                            Row(Modifier.padding(16.dp)) {
                                Text(t.title, Modifier.weight(1f))
                                Text(if (t.done) "✓" else "")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Task") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Task title") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val titleText = text.trim()
                    if (titleText.isNotEmpty()) {
                        tasks = tasks + Task(
                            id = System.currentTimeMillis(), 
                            title = titleText, 
                            dayOffset = dayOffset
                        )
                    }
                    showAdd = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
