package com.example.dayreview.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.time.LocalDate

data class Task(val id: Long, val title: String, val date: LocalDate, val done: Boolean = false)

@Composable
fun TodayScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAdd by remember { mutableStateOf(false) }
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    val todaysTasks = tasks.filter { it.date == selectedDate }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("DayReview — ${selectedDate}") },
                actions = {
                    TextButton(onClick = { selectedDate = selectedDate.minusDays(1) }) { Text("Prev") }
                    TextButton(onClick = { selectedDate = selectedDate.plusDays(1) }) { Text("Next") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Text("+") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            if (todaysTasks.isEmpty()) {
                Text("No tasks yet for today.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text("Tap + to add one.", style = MaterialTheme.typography.bodySmall)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(todaysTasks, key = { it.id }) { t ->
                        ElevatedCard {
                            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
        var text by remember { mutableStateOf(TextFieldValue("")) }
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
                    val title = text.text.trim()
                    if (title.isNotEmpty()) {
                        tasks = tasks + Task(
                            id = System.currentTimeMillis(),
                            title = title,
                            date = selectedDate
                        )
                    }
                    showAdd = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
