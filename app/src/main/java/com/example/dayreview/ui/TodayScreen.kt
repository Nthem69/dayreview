package com.example.dayreview.ui

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dayreview.DayReviewViewModel
import com.example.dayreview.data.TaskEntity
import com.example.dayreview.data.HabitEntity
import com.example.dayreview.data.MoodConfigEntity
import com.example.dayreview.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import kotlin.random.Random
import com.example.dayreview.ui.theme.HabitColors
import com.example.dayreview.ui.theme.MegaPalette
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppTab { Plan, Habits, Tracker }

@Composable
fun TodayScreen(viewModel: DayReviewViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val ratingsMap by viewModel.ratings.collectAsState()
    val ghostTasks by viewModel.ghostTasks.collectAsState() 
    val moodConfigs by viewModel.moodConfigs.collectAsState()
    val revealedItemId by viewModel.revealedItemId.collectAsState()

    val today = remember { LocalDate.now() }
    val currentTime = remember { LocalTime.now() }
    var currentTab by remember { mutableStateOf(AppTab.Plan) }
    var showSettings by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val isToday = selectedDate == today
    val isEditable = selectedDate >= today

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
        Box(modifier = Modifier.fillMaxSize().clickable { viewModel.setRevealedItem(null) }) {
            Column(
                modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                TopHeader(selectedDate, { showSettings = true }) { showMonthPicker = true }
                Spacer(modifier = Modifier.height(12.dp))
                val ratingVisuals = ratingsMap.mapValues { entry -> moodConfigs.find { it.id == entry.value } }
                MonthCalendar(selectedDate, today, ratingVisuals) { viewModel.setDate(it) }
                Spacer(modifier = Modifier.height(12.dp))
                val isRated = ratingsMap.containsKey(today)
                val isTimeToShow = currentTime.hour >= 14
                AnimatedVisibility(visible = isToday && !isRated && isTimeToShow) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("How's your day!!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            moodConfigs.forEach { config -> if(config.isVisible) MoodFaceButton(config) { viewModel.setRating(config.id) } }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                TabSegmentControl(currentTab) { currentTab = it }
                Spacer(modifier = Modifier.height(12.dp))
                Crossfade(targetState = currentTab, label = "Tab") { tab ->
                    when (tab) {
                        AppTab.Plan -> PlanContent(
                            tasks = tasks, ghostTasks = ghostTasks, isEditable = isEditable,
                            revealedId = revealedItemId, onReveal = { viewModel.setRevealedItem(it) },
                            onCheck = { viewModel.toggleTask(it) }, onUncheck = { viewModel.toggleTask(it) },
                            onDelete = { taskToDelete = it }, onEdit = { if(isEditable) taskToEdit = it }
                        )
                        AppTab.Habits -> HabitsContent(
                            habits = habits, revealedId = revealedItemId, onReveal = { viewModel.setRevealedItem(it) },
                            onToggle = { viewModel.toggleHabit(it.id) }, onDelete = { habitToDelete = it }, onEdit = { habitToEdit = it }
                        )
                        AppTab.Tracker -> TrackerContent()
                    }
                }
            }
        }
    }
    
    MonthPickerBottomSheet(showMonthPicker, YearMonth.of(selectedDate.year, selectedDate.month), { showMonthPicker = false }, { viewModel.setYearMonth(it) })

    if (habitToDelete != null) { AlertDialog(onDismissRequest = { habitToDelete = null }, title = { Text("Delete Habit?", color = Color.Black) }, text = { Text("This will remove the habit and all its history.", color = Color.Gray) }, confirmButton = { Button(onClick = { viewModel.deleteHabit(habitToDelete!!); habitToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete", color = Color.White) } }, dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("Cancel", color = Color.Gray) } }, containerColor = Color.White) }
    if (taskToDelete != null) { AlertDialog(onDismissRequest = { taskToDelete = null }, title = { Text("Delete Task?", color = Color.Black) }, confirmButton = { Button(onClick = { viewModel.deleteTask(taskToDelete!!); taskToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete", color = Color.White) } }, dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Cancel", color = Color.Gray) } }, containerColor = Color.White) }
    if (showAddTaskDialog) { CustomAddTaskDialog(onDismiss = { showAddTaskDialog = false }, onAdd = { title, time -> viewModel.addTask(title, time); showAddTaskDialog = false }) }
    if (showAddHabitDialog) { CustomAddHabitDialog(onDismiss = { showAddHabitDialog = false }, onAdd = { title, color -> viewModel.addHabit(title, color.toArgb()); showAddHabitDialog = false }) }
    if (taskToEdit != null) { CustomAddTaskDialog(initialName = taskToEdit!!.title, initialTime = taskToEdit!!.time, isEditMode = true, onDismiss = { taskToEdit = null }, onAdd = { title, time -> viewModel.updateTaskDetails(taskToEdit!!, title, time); taskToEdit = null }) }
    if (habitToEdit != null) { CustomAddHabitDialog(initialName = habitToEdit!!.title, initialColor = Color(habitToEdit!!.colorArgb), isEditMode = true, onDismiss = { habitToEdit = null }, onAdd = { title, color -> viewModel.updateHabit(habitToEdit!!.copy(title = title, colorArgb = color.toArgb())); habitToEdit = null }) }
}

@Composable
fun MonthCalendar(displayedDate: LocalDate, today: LocalDate, ratedDays: Map<LocalDate, MoodConfigEntity?>, onDateSelected: (LocalDate) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().height(280.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(24.dp)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(text = day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val daysInMonth = displayedDate.lengthOfMonth()
        val startOffset = displayedDate.withDayOfMonth(1).dayOfWeek.value % 7 
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
            repeat(6) { weekIndex ->
                Row(modifier = Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(7) { dayIndex ->
                        val totalIndex = weekIndex * 7 + dayIndex
                        val dayNum = totalIndex - startOffset + 1
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            if (totalIndex >= startOffset && dayNum <= daysInMonth) {
                                val cellDate = displayedDate.withDayOfMonth(dayNum)
                                val rating = ratedDays[cellDate]
                                val isSelected = cellDate == displayedDate
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).clip(CircleShape).background(when { rating != null -> Color(rating.colorArgb); isSelected -> Color.Black; else -> Color.Transparent }).clickable { onDateSelected(cellDate) }) {
                                    Text(text = "$dayNum", fontSize = 13.sp, fontWeight = if (cellDate == today) FontWeight.Black else FontWeight.Medium, color = if (rating != null || isSelected) Color.White else Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... Reused Components (Sleek Dialogs, Swipe, etc) ...
@Composable
fun CustomAddTaskDialog(initialName: String = "", initialTime: String? = null, isEditMode: Boolean = false, onDismiss: () -> Unit, onAdd: (String, String?) -> Unit) { var name by remember(initialName) { mutableStateOf(initialName) }; var time by remember(initialTime) { mutableStateOf(initialTime) }; val context = LocalContext.current; val timePickerDialog = TimePickerDialog(context, { _, h, m -> val amPm = if (h < 12) "AM" else "PM"; val hourDisplay = if (h % 12 == 0) 12 else h % 12; time = String.format("%d:%02d %s", hourDisplay, m, amPm) }, 12, 0, false); Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) { Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(24.dp)) { Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(if(isEditMode) "Edit Task" else "Add Task", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black); SleekTextField(value = name, onValueChange = { if (it.length <= 60) name = it }, placeholder = "Enter task name...", modifier = Modifier.fillMaxWidth()) }; Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Time", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black); Box(modifier = Modifier.fillMaxWidth()) { SleekTextField(value = time ?: "", onValueChange = {}, placeholder = "Select time", enabled = false, modifier = Modifier.fillMaxWidth(), onClick = { timePickerDialog.show() }) } }; Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) { TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel", color = Color.Black, fontWeight = FontWeight.SemiBold) }; Spacer(modifier = Modifier.width(12.dp)); Button(onClick = { if (name.isNotBlank()) onAdd(name, time) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) { Text(if(isEditMode) "Save" else "Add") } } } } } }
@Composable
fun CustomAddHabitDialog(initialName: String = "", initialColor: Color? = null, isEditMode: Boolean = false, onDismiss: () -> Unit, onAdd: (String, Color) -> Unit) { var name by remember(initialName) { mutableStateOf(initialName) }; var selectedColor by remember(initialColor) { mutableStateOf(initialColor ?: HabitColors[0]) }; var showColorPicker by remember { mutableStateOf(false) }; Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) { Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(24.dp)) { Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(if(isEditMode) "Edit Habit" else "Add Habit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black); SleekTextField(value = name, onValueChange = { if (it.length <= 40) name = it }, placeholder = "Enter habit name...", modifier = Modifier.fillMaxWidth()) }; Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("Color", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { HabitColors.forEach { color -> val isSelected = selectedColor == color; Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color).then(if(isSelected) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier).clickable { selectedColor = color }) }; Box(modifier = Modifier.size(36.dp).border(1.dp, Color(0xFFE0E0E0), CircleShape).clickable { showColorPicker = true }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "More", tint = Color.Black, modifier = Modifier.size(18.dp)) } } }; Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) { TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel", color = Color.Black, fontWeight = FontWeight.SemiBold) }; Spacer(modifier = Modifier.width(12.dp)); Button(onClick = { if (name.isNotBlank()) onAdd(name, selectedColor) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) { Text(if(isEditMode) "Save" else "Add") } } } } }; if (showColorPicker) { Dialog(onDismissRequest = { showColorPicker = false }) { Card(colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Text("Select Color", fontWeight = FontWeight.Bold, color = Color.Black); Spacer(Modifier.height(12.dp)); LazyVerticalGrid(columns = GridCells.Adaptive(40.dp), modifier = Modifier.height(200.dp)) { items(MegaPalette.size) { i -> Box(modifier = Modifier.size(40.dp).padding(4.dp).clip(CircleShape).background(MegaPalette[i]).clickable { selectedColor = MegaPalette[i]; showColorPicker = false }) } } } } } } }
@Composable
fun TopHeader(currentDate: LocalDate, onSettingsClick: () -> Unit, onMonthClick: () -> Unit) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5))) { Icon(Icons.Default.Settings, "Settings", tint = Color.Black) }; Box { Surface(shape = RoundedCornerShape(50), color = Color(0xFFF5F5F5), modifier = Modifier.height(40.dp).clickable { onMonthClick() }) { Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Text(currentDate.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault()), fontWeight = FontWeight.SemiBold, color = Color.Black); Spacer(modifier = Modifier.width(4.dp)); Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black) } } } } }
@Composable
fun MoodFaceButton(config: MoodConfigEntity, onClick: () -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(config.colorArgb)).clickable { onClick() }) { Icon(painter = painterResource(config.iconResId), contentDescription = config.label, tint = Color.White, modifier = Modifier.size(24.dp)) }; Spacer(modifier = Modifier.height(4.dp)); Text(text = config.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(config.colorArgb), textAlign = TextAlign.Center) } }
@Composable
fun MonthPickerBottomSheet(open: Boolean, selected: YearMonth, onDismiss: () -> Unit, onMonthSelected: (YearMonth) -> Unit) { if (!open) return; ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White, dragHandle = { BottomSheetDefaults.DragHandle() }) { var year by remember { mutableStateOf(selected.year) }; Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) { Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Select Month", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, color = Color.Black), modifier = Modifier.weight(1f)); Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { IconButton(onClick = { year-- }) { Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.Black) }; Text(text = year.toString(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, color = Color.Black)); IconButton(onClick = { year++ }) { Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Black) } } }; Spacer(Modifier.height(12.dp)); val months = remember { Month.values().toList() }; LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) { items(months) { m -> val isSelected = (m.value == selected.month.value && year == selected.year); Surface(color = if (isSelected) Color.Black else Color(0xFFF0F0F0), contentColor = if (isSelected) Color.White else Color.Black, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(14.dp)).clickable { onMonthSelected(YearMonth.of(year, m)); onDismiss() }) { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(text = m.name.take(3), fontWeight = FontWeight.SemiBold, fontSize = 15.sp) } } } } } } }
@Composable
fun TabSegmentControl(selected: AppTab, onSelect: (AppTab) -> Unit) { Row(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) { AppTab.values().forEach { tab -> val isSelected = selected == tab; Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White else Color.Transparent).clickable { onSelect(tab) }) { Text(tab.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.Black else Color.Gray, fontSize = 14.sp) } } } }
@Composable
fun TrackerContent() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tracker coming soon...", color = Color.Gray) } }
@Composable
fun SwipeActionBox(itemId: String, revealedId: String?, onReveal: (String?) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) { val menuWidth = 120.dp; val menuWidthPx = with(LocalDensity.current) { menuWidth.toPx() }; val offset = remember { Animatable(0f) }; val scope = rememberCoroutineScope(); LaunchedEffect(revealedId) { if (revealedId != itemId && offset.value < 0) { offset.animateTo(0f) } }; Box(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(16.dp))) { Row(modifier = Modifier.align(Alignment.CenterEnd).width(menuWidth).fillMaxHeight()) { Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4285F4)).clickable { onReveal(null); onEdit() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) }; Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEF5350)).clickable { onReveal(null); onDelete() }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Delete, "Delete", tint = Color.White) } }; Box(modifier = Modifier.offset { IntOffset(offset.value.roundToInt(), 0) }.background(Color.White).draggable(orientation = Orientation.Horizontal, state = rememberDraggableState { delta -> val newValue = (offset.value + delta).coerceIn(-menuWidthPx, 0f); scope.launch { offset.snapTo(newValue) } }, onDragStarted = { onReveal(itemId) }, onDragStopped = { val target = if (offset.value < -menuWidthPx / 2) -menuWidthPx else 0f; scope.launch { offset.animateTo(target) }; if (target == 0f) onReveal(null) })) { content() } } }
@Composable
fun SleekTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: (() -> Unit)? = null) { BasicTextField(value = value, onValueChange = onValueChange, enabled = enabled, textStyle = TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Normal), singleLine = true, modifier = modifier.height(42.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)).background(Color.White).clickable(enabled = onClick != null) { onClick?.invoke() }.padding(horizontal = 12.dp), decorationBox = { innerTextField -> Box(contentAlignment = Alignment.CenterStart) { if (value.isEmpty()) Text(placeholder, color = Color(0xFF9E9E9E), fontSize = 15.sp); innerTextField() } }) }
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanContent(tasks: List<TaskEntity>, ghostTasks: List<TaskEntity>, isEditable: Boolean, revealedId: String?, onReveal: (String?) -> Unit, onCheck: (TaskEntity) -> Unit, onUncheck: (TaskEntity) -> Unit, onDelete: (TaskEntity) -> Unit, onEdit: (TaskEntity) -> Unit) { val haptic = LocalHapticFeedback.current; LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) { if (ghostTasks.isNotEmpty() && isEditable) { item { Text("Unfinished Yesterday", style = MaterialTheme.typography.labelMedium, color = Color.Gray) }; items(ghostTasks.size) { i -> val task = ghostTasks[i]; Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF9F9F9)).padding(16.dp).alpha(0.6f), verticalAlignment = Alignment.CenterVertically) { Text(task.title, color = Color.Gray, modifier = Modifier.weight(1f)); Icon(Icons.Default.Add, "Move", tint = Color.Black) } }; item { Spacer(modifier = Modifier.height(8.dp)) } }; if (tasks.isEmpty() && ghostTasks.isEmpty()) { item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No tasks today.", color = Color.LightGray) } } } else { items(tasks.size, key = { tasks[it].id }) { i -> val task = tasks[i]; SwipeActionBox(itemId = "task_${task.id}", revealedId = revealedId, onReveal = onReveal, onEdit = { onEdit(task) }, onDelete = { onDelete(task) }) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF8F9FA)).combinedClickable(onClick = { if (!task.isDone) onCheck(task) }, onLongClick = { if (task.isDone) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onUncheck(task) } }).padding(16.dp)) { Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (task.isDone) Color.Black else Color.Transparent, CircleShape).border(2.dp, if(task.isDone) Color.Black else Color.Gray, CircleShape), contentAlignment = Alignment.Center) { if (task.isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }; Spacer(modifier = Modifier.width(16.dp)); Column { Text(task.title, color = if (task.isDone) Color.Gray else Color.Black, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis); if (task.time != null) Text(task.time, color = Color.Gray, fontSize = 12.sp) } } } } } } }
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsContent(habits: List<HabitEntity>, revealedId: String?, onReveal: (String?) -> Unit, onToggle: (HabitEntity) -> Unit, onDelete: (HabitEntity) -> Unit, onEdit: (HabitEntity) -> Unit) { val haptic = LocalHapticFeedback.current; if (habits.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No habits yet.", color = Color.LightGray) } }; LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) { items(habits.size) { i -> val habit = habits[i]; val color = Color(habit.colorArgb); val totalFilled = habit.history.count { it }; val random = Random(habit.id.hashCode() + habit.title.hashCode()); val displayPattern = BooleanArray(30); val indices = (0 until 30).toMutableList(); repeat(totalFilled.coerceAtMost(30)) { if (indices.isNotEmpty()) { val randIndex = indices.removeAt(random.nextInt(indices.size)); displayPattern[randIndex] = true } }; SwipeActionBox(itemId = "habit_${habit.id}", revealedId = revealedId, onReveal = onReveal, onEdit = { onEdit(habit) }, onDelete = { onDelete(habit) }) { Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp)).combinedClickable(onClick = { if (!habit.isDoneToday) onToggle(habit) }, onLongClick = { if (habit.isDoneToday) { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onToggle(habit) } }).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text(habit.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(modifier = Modifier.height(10.dp)); Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { repeat(5) { r -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(7) { c -> val isVisible = !((r == 0 && c < 2) || (r == 4 && c > 4)); if (isVisible) { val linearIdx = (r * 7 + c) - 2; val isFilled = if(linearIdx in displayPattern.indices) displayPattern[linearIdx] else false; Box(modifier = Modifier.width(12.dp).height(6.dp).clip(RoundedCornerShape(2.dp)).background(if (isFilled) color else Color(0xFFF0F0F0))) } else { Box(modifier = Modifier.width(12.dp).height(6.dp).background(Color.Transparent)) } } } } } }; Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("🔥 ${habit.streak}", fontSize = 12.sp, color = Color.Gray); Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (habit.isDoneToday) color else Color(0xFFF0F0F0)).clickable { onToggle(habit) }, contentAlignment = Alignment.Center) { if (habit.isDoneToday) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) } } } } } } }
fun Modifier.alpha(value: Float) = this.then(Modifier.background(Color.Transparent.copy(alpha = 1f - value)))
