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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.FaceRetouchingNatural
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dayreview.DayReviewViewModel
import com.example.dayreview.data.TaskEntity
import com.example.dayreview.data.HabitEntity
import com.example.dayreview.data.MoodConfigEntity
import com.example.dayreview.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

// --- GLOBAL CONSTANTS (Fixed Scope Issue) ---
val HabitColors = listOf(
    Color(0xFF4FB3FF), // Blue
    Color(0xFFFF6F3B), // Orange
    Color(0xFF4CAF50), // Green
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0)  // Purple
)

enum class AppTab { Plan, Habits, Tracker }

// --- MAIN SCREEN ---
@Composable
fun TodayScreen(viewModel: DayReviewViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val ratingsMap by viewModel.ratings.collectAsState()
    val ghostTasks by viewModel.ghostTasks.collectAsState() 
    val moodConfigs by viewModel.moodConfigs.collectAsState()

    val today = remember { LocalDate.now() }
    val currentTime = remember { LocalTime.now() }
    var currentTab by remember { mutableStateOf(AppTab.Plan) }
    var showSettings by remember { mutableStateOf(false) }

    // Dialog States
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }

    val isToday = selectedDate == today
    val isEditable = selectedDate >= today

    // Settings Navigation
    if (showSettings) {
        SettingsScreen(viewModel) { showSettings = false }
        return
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            val onClickAction: () -> Unit = when (currentTab) {
                AppTab.Plan -> { { showAddTaskDialog = true } }
                AppTab.Habits -> { { showAddHabitDialog = true } }
                AppTab.Tracker -> { { } }
            }
            if (isEditable || currentTab != AppTab.Plan) {
                FloatingActionButton(onClick = onClickAction, containerColor = Color.Black, contentColor = Color.White, shape = CircleShape) { 
                    Icon(Icons.Default.Add, "Add") 
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TopHeader(selectedDate, { showSettings = true }) { newMonth -> viewModel.setDate(selectedDate.withMonth(newMonth.value).withDayOfMonth(1)) }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Calendar
            val ratingVisuals = ratingsMap.mapValues { entry -> moodConfigs.find { it.id == entry.value } }
            MonthCalendar(selectedDate, today, ratingVisuals) { viewModel.setDate(it) }
            Spacer(modifier = Modifier.height(12.dp))

            // Rating
            val isRated = ratingsMap.containsKey(today)
            val isTimeToShow = currentTime.hour >= 14
            
            AnimatedVisibility(visible = isToday && !isRated && isTimeToShow) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("How's your day!!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        moodConfigs.forEach { config -> 
                            if(config.isVisible) MoodFaceButton(config) { viewModel.setRating(config.id) } 
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            TabSegmentControl(currentTab) { currentTab = it }
            Spacer(modifier = Modifier.height(12.dp))

            Crossfade(targetState = currentTab, label = "Tab") { tab ->
                when (tab) {
                    AppTab.Plan -> PlanContent(
                        tasks = tasks,
                        ghostTasks = ghostTasks,
                        isEditable = isEditable,
                        onCheck = { viewModel.toggleTask(it) },
                        onUncheck = { viewModel.toggleTask(it) },
                        onEdit = { if(isEditable) taskToEdit = it }
                    )
                    AppTab.Habits -> HabitsContent(
                        habits = habits,
                        onToggle = { viewModel.toggleHabit(it.id) },
                        onEdit = { habitToEdit = it }
                    )
                    AppTab.Tracker -> TrackerContent()
                }
            }
        }
    }
    
    // --- DIALOGS (Cleanly separated from Composable tree) ---
    if (showAddTaskDialog) { 
        TaskDialog("New Task", "", { showAddTaskDialog = false }) { txt -> viewModel.addTask(txt); showAddTaskDialog = false } 
    }
    if (showAddHabitDialog) { 
        TaskDialog("New Habit", "", { showAddHabitDialog = false }) { txt -> viewModel.addHabit(txt); showAddHabitDialog = false } 
    }
    if (taskToEdit != null) { 
        TaskDialog("Edit Task", taskToEdit!!.title, { taskToEdit = null }) { txt -> viewModel.updateTaskTitle(taskToEdit!!, txt); taskToEdit = null } 
    }
    if (habitToEdit != null) { 
        HabitEditDialog(habitToEdit!!, { habitToEdit = null }) { title, color -> 
            viewModel.updateHabit(habitToEdit!!.copy(title = title, colorArgb = color.toArgb()))
            habitToEdit = null
        }
    }
}

// --- COMPONENTS ---

@Composable
fun MoodFaceButton(config: MoodConfigEntity, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(config.colorArgb)).clickable { onClick() }) {
            // Using painterResource because icons are loaded from DB/Drawable
            Icon(painter = painterResource(config.iconResId), contentDescription = config.label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = config.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(config.colorArgb), textAlign = TextAlign.Center)
    }
}

@Composable
fun HabitEditDialog(habit: HabitEntity, onDismiss: () -> Unit, onConfirm: (String, Color) -> Unit) {
    var text by remember { mutableStateOf(habit.title) }
    var selectedColor by remember { mutableStateOf(Color(habit.colorArgb)) }
    
    AlertDialog(
        onDismissRequest = onDismiss, 
        title = { Text("Edit Habit", color = Color.Black) }, 
        text = { 
            Column { 
                OutlinedTextField(
                    value = text, 
                    onValueChange = { text = it }, 
                    label = { Text("Title", color = Color.Gray) }, 
                    singleLine = true, 
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, cursorColor = Color.Black)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Color Picker Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                    HabitColors.forEach { color -> 
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, if (selectedColor == color) Color.Black else Color.Transparent, CircleShape)
                                .clickable { selectedColor = color }
                        ) 
                    } 
                } 
            } 
        }, 
        confirmButton = { 
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text, selectedColor) }, 
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) { Text("Save", color = Color.White) } 
        }, 
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }, 
        containerColor = Color.White
    )
}

// ... (Rest of existing components: PlanContent, HabitsContent, Calendar, etc. remain unchanged) ...
@Composable
fun TopHeader(currentDate: LocalDate, onSettingsClick: () -> Unit, onMonthSelected: (Month) -> Unit) { 
    var menuExpanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
        IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5))) {
            Icon(Icons.Default.Menu, "Settings", tint = Color.Black)
        }
        Box { 
            Surface(shape = RoundedCornerShape(50), color = Color(0xFFF5F5F5), modifier = Modifier.height(40.dp).clickable { menuExpanded = true }) { 
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { 
                    Text(currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()), fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black) 
                } 
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(Color.White)) { 
                Month.values().forEach { month -> DropdownMenuItem(text = { Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault()), color = Color.Black) }, onClick = { onMonthSelected(month); menuExpanded = false }) } 
            } 
        } 
    } 
}

@Composable
fun MonthCalendar(displayedDate: LocalDate, today: LocalDate, ratedDays: Map<LocalDate, MoodConfigEntity?>, onDateSelected: (LocalDate) -> Unit) { Column(modifier = Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(24.dp)).padding(12.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { day -> Text(day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) } }; Spacer(modifier = Modifier.height(4.dp)); val daysInMonth = displayedDate.lengthOfMonth(); val startOffset = displayedDate.withDayOfMonth(1).dayOfWeek.value % 7; LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize(), userScrollEnabled = false) { items(42) { index -> val dayNum = index - startOffset + 1; if (index < startOffset || dayNum > daysInMonth) { Box(modifier = Modifier.size(30.dp)) } else { val cellDate = displayedDate.withDayOfMonth(dayNum); val rating = ratedDays[cellDate]; val isSelected = cellDate == displayedDate; Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).clip(rating?.let { CircleShape } ?: CircleShape).background(when { rating != null -> Color(rating.colorArgb); isSelected -> Color.Black; else -> Color.Transparent }).clickable { onDateSelected(cellDate) }) { Text("$dayNum", fontSize = 12.sp, fontWeight = if (cellDate==today) FontWeight.ExtraBold else FontWeight.Medium, color = if (rating != null || isSelected) Color.White else Color.Black) } } } } } }
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanContent(tasks: List<TaskEntity>, ghostTasks: List<TaskEntity>, isEditable: Boolean, onCheck: (TaskEntity) -> Unit, onUncheck: (TaskEntity) -> Unit, onEdit: (TaskEntity) -> Unit) { val haptic = LocalHapticFeedback.current; LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) { if (ghostTasks.isNotEmpty() && isEditable) { item { Text("Unfinished Yesterday", style = MaterialTheme.typography.labelMedium, color = Color.Gray) }; items(ghostTasks.size) { i -> val task = ghostTasks[i]; Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF9F9F9)).padding(16.dp).alpha(0.6f), verticalAlignment = Alignment.CenterVertically) { Text(task.title, color = Color.Gray, modifier = Modifier.weight(1f)); Icon(Icons.Default.Add, "Move", tint = Color.Black) } }; item { Spacer(modifier = Modifier.height(8.dp)) } }; if (tasks.isEmpty() && ghostTasks.isEmpty()) { item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No tasks today.", color = Color.LightGray) } } } else { items(tasks.size, key = { tasks[it].id }) { i -> val task = tasks[i]; val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onEdit(task); false } else false }); SwipeToDismissBox(state = dismissState, backgroundContent = { Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.LightGray).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) } }, enableDismissFromStartToEnd = false) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF8F9FA)).combinedClickable(onClick = { if (!task.isDone) onCheck(task) }, onLongClick = { if (task.isDone) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onUncheck(task) } }).padding(16.dp)) { Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (task.isDone) Color.Black else Color.Transparent, CircleShape).border(2.dp, if(task.isDone) Color.Black else Color.Gray, CircleShape), contentAlignment = Alignment.Center) { if (task.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }; Spacer(modifier = Modifier.width(16.dp)); Text(task.title, color = if (task.isDone) Color.Gray else Color.Black, style = MaterialTheme.typography.bodyLarge) } } } } } }
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsContent(habits: List<HabitEntity>, onToggle: (HabitEntity) -> Unit, onEdit: (HabitEntity) -> Unit) { if (habits.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No habits yet.", color = Color.LightGray) } }; LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) { items(habits.size) { i -> val habit = habits[i]; val color = Color(habit.colorArgb); Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp)).combinedClickable(onClick = { }, onLongClick = { onEdit(habit) }).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text(habit.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black); Spacer(modifier = Modifier.height(10.dp)); Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { repeat(5) { r -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(7) { c -> val isVisible = !((r == 0 && c < 2) || (r == 4 && c > 4)); if (isVisible) { val historyIndex = (r * 7 + c) - 2; if (historyIndex >= 0 && historyIndex < habit.history.size) { val isFilled = habit.history[historyIndex]; Box(modifier = Modifier.width(12.dp).height(6.dp).clip(RoundedCornerShape(2.dp)).background(if (isFilled) color else Color(0xFFF0F0F0))) } else { Box(modifier = Modifier.width(12.dp).height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF0F0F0))) } } else { Box(modifier = Modifier.width(12.dp).height(6.dp).background(Color.Transparent)) } } } } } }; Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("🔥 ${habit.streak}", fontSize = 12.sp, color = Color.Gray); Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (habit.isDoneToday) color else Color(0xFFF0F0F0)).clickable { onToggle(habit) }, contentAlignment = Alignment.Center) { if (habit.isDoneToday) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) } } } } } }
@Composable
fun TabSegmentControl(selected: AppTab, onSelect: (AppTab) -> Unit) { Row(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) { AppTab.values().forEach { tab -> val isSelected = selected == tab; Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White else Color.Transparent).clickable { onSelect(tab) }) { Text(tab.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.Black else Color.Gray, fontSize = 14.sp) } } } }
@Composable
fun TrackerContent() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tracker coming soon...", color = Color.Gray) } }
@Composable
fun TaskDialog(title: String, initialText: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var text by remember { mutableStateOf(initialText) }; AlertDialog(onDismissRequest = onDismiss, title = { Text(title, color = Color.Black) }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Description", color = Color.Gray) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, cursorColor = Color.Black)) }, confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Save", color = Color.White) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }, containerColor = Color.White) }
fun Modifier.alpha(value: Float) = this.then(Modifier.background(Color.Transparent.copy(alpha = 1f - value)))
