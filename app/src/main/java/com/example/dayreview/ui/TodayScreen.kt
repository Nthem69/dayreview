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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.* // For Faces
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dayreview.DayReviewViewModel
import com.example.dayreview.data.TaskEntity
import com.example.dayreview.data.HabitEntity
import com.example.dayreview.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

// --- MOOD CONFIGURATION ---
data class MoodItem(
    val id: String, 
    val defaultLabel: String, 
    val color: Color, 
    val icon: ImageVector
)

// The 5-Point Scale
val AllMoods = listOf(
    MoodItem("awful", "AWFUL", Color(0xFF9C27B0), Icons.Rounded.SentimentVeryDissatisfied), // Purple
    MoodItem("bad", "BAD", Color(0xFFF44336), Icons.Rounded.SentimentDissatisfied),         // Red
    MoodItem("okay", "OKAY", Color(0xFFFFC107), Icons.Rounded.SentimentNeutral),            // Yellow
    MoodItem("good", "GOOD", Color(0xFF76FF03), Icons.Rounded.SentimentSatisfied),          // Lime Green
    MoodItem("custom", "???", Color(0xFF9E9E9E), Icons.Rounded.FaceRetouchingNatural)       // Grey
)

val HabitColors = listOf(MoodBlue, MoodOrange, Color(0xFF4CAF50), Color(0xFFE91E63), Color(0xFF9C27B0))
enum class AppTab { Plan, Habits, Tracker }

@Composable
fun TodayScreen(viewModel: DayReviewViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val ratingsMap by viewModel.ratings.collectAsState()
    val ghostTasks by viewModel.ghostTasks.collectAsState() 

    val today = remember { LocalDate.now() }
    val currentTime = remember { LocalTime.now() }
    var currentTab by remember { mutableStateOf(AppTab.Plan) }

    // Mood State (InMemory for now, ideally DB)
    var visibleMoodIds by remember { mutableStateOf(setOf("awful", "bad", "okay", "good", "custom")) }
    var customLabel by remember { mutableStateOf("???") }
    var showMoodConfig by remember { mutableStateOf(false) }

    // Dialogs
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }

    val isToday = selectedDate == today
    val isEditable = selectedDate >= today

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
            TopHeader(selectedDate) { newMonth -> viewModel.setDate(selectedDate.withMonth(newMonth.value).withDayOfMonth(1)) }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Calendar - Uses Colors Only
            val ratingVisuals = ratingsMap.mapValues { entry -> AllMoods.find { it.id == entry.value } }
            MonthCalendar(selectedDate, today, ratingVisuals) { viewModel.setDate(it) }
            Spacer(modifier = Modifier.height(12.dp))

            // Rating Section (Conditional)
            val isRated = ratingsMap.containsKey(today)
            val isTimeToShow = currentTime.hour >= 14
            
            AnimatedVisibility(visible = isToday && !isRated && isTimeToShow) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("How's your day!!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // RENDER 5 ICONS
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AllMoods.filter { it.id in visibleMoodIds }.forEach { mood ->
                                val label = if (mood.id == "custom") customLabel else mood.defaultLabel
                                MoodFaceButton(mood, label) { viewModel.setRating(mood.id) }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    // The Pencil Icon (Top Right of Rating Area)
                    IconButton(
                        onClick = { showMoodConfig = true },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Moods", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
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
    
    // --- DIALOGS ---
    if (showMoodConfig) {
        AlertDialog(
            onDismissRequest = { showMoodConfig = false },
            title = { Text("Configure Moods", color = Color.Black) },
            text = {
                Column {
                    Text("Select visible moods:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    AllMoods.forEach { mood ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                            // Toggle Logic
                            visibleMoodIds = if (visibleMoodIds.contains(mood.id)) visibleMoodIds - mood.id else visibleMoodIds + mood.id
                        }) {
                            Checkbox(checked = visibleMoodIds.contains(mood.id), onCheckedChange = null)
                            Text(mood.defaultLabel, modifier = Modifier.padding(start = 8.dp), color = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = customLabel, 
                        onValueChange = { customLabel = it }, 
                        label = { Text("Custom Label (Grey)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )
                }
            },
            confirmButton = { Button(onClick = { showMoodConfig = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Done", color = Color.White) } },
            containerColor = Color.White
        )
    }

    if (showAddTaskDialog) { TaskDialog("New Task", "", { showAddTaskDialog = false }) { txt -> viewModel.addTask(txt); showAddTaskDialog = false } }
    if (showAddHabitDialog) { TaskDialog("New Habit", "", { showAddHabitDialog = false }) { txt -> viewModel.addHabit(txt); showAddHabitDialog = false } }
    if (taskToEdit != null) { TaskDialog("Edit Task", taskToEdit!!.title, { taskToEdit = null }) { txt -> viewModel.updateTaskTitle(taskToEdit!!, txt); taskToEdit = null } }
    if (habitToEdit != null) { 
        HabitEditDialog(habitToEdit!!, { habitToEdit = null }) { title, color -> 
            viewModel.updateHabit(habitToEdit!!.copy(title = title, colorArgb = color.toArgb()))
            habitToEdit = null
        }
    }
}

// --- NEW COMPONENT: MOOD FACE BUTTON ---
@Composable
fun MoodFaceButton(mood: MoodItem, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Icon Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp) // Large tap target
                .clip(CircleShape)
                .background(mood.color)
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = mood.icon,
                contentDescription = label,
                tint = Color.Black.copy(alpha = 0.8f), // Dark icons on colored bg
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Tiny Label
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = mood.color, // Label matches face color
            textAlign = TextAlign.Center
        )
    }
}

// --- PLAN CONTENT (Strict Press-and-Hold) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanContent(tasks: List<TaskEntity>, ghostTasks: List<TaskEntity>, isEditable: Boolean, onCheck: (TaskEntity) -> Unit, onUncheck: (TaskEntity) -> Unit, onEdit: (TaskEntity) -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        if (ghostTasks.isNotEmpty() && isEditable) {
            item { Text("Unfinished Yesterday", style = MaterialTheme.typography.labelMedium, color = Color.Gray) }
            items(ghostTasks.size) { i ->
                val task = ghostTasks[i]
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF9F9F9)).padding(16.dp).alpha(0.6f), verticalAlignment = Alignment.CenterVertically) {
                    Text(task.title, color = Color.Gray, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Add, "Move", tint = Color.Black) 
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
        if (tasks.isEmpty() && ghostTasks.isEmpty()) { 
             item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No tasks today.", color = Color.LightGray) } }
        } else {
            items(tasks.size, key = { tasks[it].id }) { i ->
                val task = tasks[i]
                val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onEdit(task); false } else false })
                SwipeToDismissBox(state = dismissState, backgroundContent = { Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.LightGray).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) } }, enableDismissFromStartToEnd = false) {
                    
                    // ROW CONTAINER
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8F9FA))
                            // COMBINED CLICKABLE: Strictly separated logic
                            .combinedClickable(
                                onClick = { 
                                    if (!task.isDone) onCheck(task) 
                                    // If done, tap does nothing.
                                },
                                onLongClick = { 
                                    if (task.isDone) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onUncheck(task)
                                    }
                                }
                            )
                            .padding(16.dp)
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (task.isDone) Color.Black else Color.Transparent, CircleShape).border(2.dp, if(task.isDone) Color.Black else Color.Gray, CircleShape), contentAlignment = Alignment.Center) { if (task.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(task.title, color = if (task.isDone) Color.Gray else Color.Black, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

// ... Rest of components (HabitContent, Calendar, etc) unchanged from previous working state ...
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsContent(habits: List<HabitEntity>, onToggle: (HabitEntity) -> Unit, onEdit: (HabitEntity) -> Unit) {
    if (habits.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No habits yet.", color = Color.LightGray) } }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        items(habits.size) { i ->
            val habit = habits[i]
            val color = Color(habit.colorArgb)
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp)).combinedClickable(onClick = { /* No action */ }, onLongClick = { onEdit(habit) }).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) { r ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(7) { c ->
                                    val isVisible = !((r == 0 && c < 2) || (r == 4 && c > 4))
                                    if (isVisible) {
                                        val historyIndex = (r * 7 + c) - 2
                                        if (historyIndex >= 0 && historyIndex < habit.history.size) {
                                            val isFilled = habit.history[historyIndex]
                                            Box(modifier = Modifier.width(12.dp).height(6.dp).clip(RoundedCornerShape(2.dp)).background(if (isFilled) color else Color(0xFFF0F0F0)))
                                        } else { Box(modifier = Modifier.width(12.dp).height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF0F0F0))) }
                                    } else { Box(modifier = Modifier.width(12.dp).height(6.dp).background(Color.Transparent)) }
                                }
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔥 ${habit.streak}", fontSize = 12.sp, color = Color.Gray)
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (habit.isDoneToday) color else Color(0xFFF0F0F0)).clickable { onToggle(habit) }, contentAlignment = Alignment.Center) {
                        if (habit.isDoneToday) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun TaskDialog(title: String, initialText: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var text by remember { mutableStateOf(initialText) }; AlertDialog(onDismissRequest = onDismiss, title = { Text(title, color = Color.Black) }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Description", color = Color.Gray) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, cursorColor = Color.Black)) }, confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Save", color = Color.White) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }, containerColor = Color.White) }
@Composable
fun HabitEditDialog(habit: HabitEntity, onDismiss: () -> Unit, onConfirm: (String, Color) -> Unit) { var text by remember { mutableStateOf(habit.title) }; var selectedColor by remember { mutableStateOf(Color(habit.colorArgb)) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit Habit", color = Color.Black) }, text = { Column { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Title", color = Color.Gray) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, cursorColor = Color.Black)); Spacer(modifier = Modifier.height(16.dp)); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { HabitColors.forEach { color -> Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(color).border(2.dp, if (selectedColor == color) Color.Black else Color.Transparent, CircleShape).clickable { selectedColor = color }) } } } }, confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text, selectedColor) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("Save", color = Color.White) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }, containerColor = Color.White) }
@Composable
fun MonthCalendar(displayedDate: LocalDate, today: LocalDate, ratedDays: Map<LocalDate, MoodItem?>, onDateSelected: (LocalDate) -> Unit) { Column(modifier = Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(24.dp)).padding(12.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { day -> Text(day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) } }; Spacer(modifier = Modifier.height(4.dp)); val daysInMonth = displayedDate.lengthOfMonth(); val startOffset = displayedDate.withDayOfMonth(1).dayOfWeek.value % 7; LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize(), userScrollEnabled = false) { items(42) { index -> val dayNum = index - startOffset + 1; if (index < startOffset || dayNum > daysInMonth) { Box(modifier = Modifier.size(30.dp)) } else { val cellDate = displayedDate.withDayOfMonth(dayNum); val rating = ratedDays[cellDate]; val isSelected = cellDate == displayedDate; Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).clip(rating?.let { CircleShape } ?: CircleShape).background(when { rating != null -> rating.color; isSelected -> Color.Black; else -> Color.Transparent }).clickable { onDateSelected(cellDate) }) { Text("$dayNum", fontSize = 12.sp, fontWeight = if (cellDate==today) FontWeight.ExtraBold else FontWeight.Medium, color = if (rating != null || isSelected) Color.White else Color.Black) } } } } } }
@Composable
fun TabSegmentControl(selected: AppTab, onSelect: (AppTab) -> Unit) { Row(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) { AppTab.values().forEach { tab -> val isSelected = selected == tab; Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White else Color.Transparent).clickable { onSelect(tab) }) { Text(tab.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.Black else Color.Gray, fontSize = 14.sp) } } } }
@Composable
fun TrackerContent() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tracker coming soon...", color = Color.Gray) } }
@Composable
fun TopHeader(currentDate: LocalDate, onMonthSelected: (Month) -> Unit) { var menuExpanded by remember { mutableStateOf(false) }; Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) { Text("=", fontWeight = FontWeight.Bold, color = Color.Black) }; Box { Surface(shape = RoundedCornerShape(50), color = Color(0xFFF5F5F5), modifier = Modifier.height(40.dp).clickable { menuExpanded = true }) { Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Text(currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()), fontWeight = FontWeight.SemiBold, color = Color.Black); Spacer(modifier = Modifier.width(4.dp)); Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black) } }; DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(Color.White)) { Month.values().forEach { month -> DropdownMenuItem(text = { Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault()), color = Color.Black) }, onClick = { onMonthSelected(month); menuExpanded = false }) } } } } }
@Composable
fun MoodButton(mood: MoodItem, onClick: () -> Unit) { Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(mood.color).clickable { onClick() }) }
fun Modifier.alpha(value: Float) = this.then(Modifier.background(Color.Transparent.copy(alpha = 1f - value)))
