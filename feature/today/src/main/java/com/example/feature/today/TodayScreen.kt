package com.example.feature.today

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TodayScreen() {
    val tasks = listOf("Welcome to DayReview!", "Tap + to add a task (coming soon)")
    Scaffold(
        topBar = { TopAppBar(title = { Text("Today") }) },
        floatingActionButton = { FloatingActionButton(onClick = {}) { Text("+") } }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            tasks.forEach {
                ElevatedCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text(it, Modifier.padding(16.dp))
                }
            }
        }
    }
}
