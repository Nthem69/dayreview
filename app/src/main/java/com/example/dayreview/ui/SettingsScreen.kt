package com.example.dayreview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.dayreview.DayReviewViewModel
import com.example.dayreview.data.MoodConfigEntity
import com.example.dayreview.R
import com.example.dayreview.ui.theme.MegaPalette // Import instead of define

val AvailableIcons = listOf(R.drawable.ic_mood_1, R.drawable.ic_mood_2, R.drawable.ic_mood_3, R.drawable.ic_mood_4, R.drawable.ic_mood_5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DayReviewViewModel, onBack: () -> Unit) {
    var showMoodCustomization by remember { mutableStateOf(false) }

    if (showMoodCustomization) {
        MoodCustomizationScreen(viewModel) { showMoodCustomization = false }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(16.dp)) {
            Text("General", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsItem(Icons.Default.Palette, "Customize Moods", Color(0xFF4CAF50)) { showMoodCustomization = true }
            SettingsItem(Icons.Default.Notifications, "Notifications", Color(0xFFFF9800)) { /* Todo */ }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Account", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsItem(Icons.Default.Person, "Profile", Color(0xFF2196F3)) { /* Todo */ }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, iconColor: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(iconColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.Black, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodCustomizationScreen(viewModel: DayReviewViewModel, onBack: () -> Unit) {
    val configs by viewModel.moodConfigs.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Customize Moods", color = Color.Black) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)) },
        containerColor = Color.White
    ) { pad ->
        LazyColumn(modifier = Modifier.padding(pad).padding(16.dp)) {
            items(configs) { config ->
                MoodConfigRow(config) { updated -> viewModel.updateMoodConfig(updated) }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
fun MoodConfigRow(config: MoodConfigEntity, onUpdate: (MoodConfigEntity) -> Unit) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(config.colorArgb)).clickable { showIconPicker = true }, contentAlignment = Alignment.Center) { Icon(painter = painterResource(config.iconResId), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(value = config.label, onValueChange = { onUpdate(config.copy(label = it)) }, modifier = Modifier.weight(1f), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, unfocusedBorderColor = Color.LightGray, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(config.colorArgb)).border(1.dp, Color.Gray, CircleShape).clickable { showColorPicker = true })
        }
    }
    if (showColorPicker) { Dialog(onDismissRequest = { showColorPicker = false }) { Card(colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Text("Select Color", fontWeight = FontWeight.Bold, color = Color.Black); Spacer(Modifier.height(12.dp)); LazyVerticalGrid(columns = GridCells.Adaptive(40.dp), modifier = Modifier.height(200.dp)) { items(MegaPalette.size) { i -> Box(modifier = Modifier.size(40.dp).padding(4.dp).clip(CircleShape).background(MegaPalette[i]).clickable { onUpdate(config.copy(colorArgb = MegaPalette[i].toArgb())); showColorPicker = false }) } } } } } }
    if (showIconPicker) { Dialog(onDismissRequest = { showIconPicker = false }) { Card(colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) { AvailableIcons.forEach { resId -> IconButton(onClick = { onUpdate(config.copy(iconResId = resId)); showIconPicker = false }) { Icon(painter = painterResource(resId), contentDescription = null, tint = Color.Black) } } } } } }
}
