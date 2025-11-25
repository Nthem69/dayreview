package com.example.dayreview.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Explicit imports to prevent build failures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class Task(
    val id: Long, 
    val title: String, 
    val dayOffset: Int, 
    val done: Boolean = false
)

@Composable
fun TodayScreen() {
    // State
    var dayOffset by rememberSaveable { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    // Helpers
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
        AddDialog(
            onDismiss = { showAdd = false },
            onConfirm = { txt ->
                if (txt.isNotBlank()) {
                    tasks = tasks + Task(System.currentTimeMillis(), txt, dayOffset)
                }
                showAdd = false
            }
        )
    }
}

@Composable
fun AddDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
