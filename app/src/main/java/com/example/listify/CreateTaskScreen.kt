package com.example.listify

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

// Helper function to get user's theme color
// fun getUserThemeColor(context: Context): Color {
//     val userProfile = context.getUserProfile()
//     return userProfile?.themeColor ?: Color(0xFFF48FB1) // Default to light pink if not set
// }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(navController: NavController, onTaskCreated: (Task) -> Unit = {}) {
    var taskName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var description by remember { mutableStateOf("") }
    var hasDuration by remember { mutableStateOf(false) } // New state for duration checkbox
    var taskNameError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text("Create New Task", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() }
                    )
                }
        ) {
            SectionHeader("Task Name")
            OutlinedTextField(
                value = taskName,
                onValueChange = {
                    taskName = it
                    if (it.isNotBlank()) taskNameError = false
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter task name") },
                isError = taskNameError,
                supportingText = {
                    if (taskNameError) Text("Task name is required", color = MaterialTheme.colorScheme.error)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = getUserThemeColor(LocalContext.current), // User's theme color
                    focusedLabelColor = getUserThemeColor(LocalContext.current) // User's theme color
                )
            )

            SectionHeader("Category")
            // Using a Column with wrapped Row elements to simulate FlowRow behavior
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf("JIRA", "Lunch", "Sync-Up", "Personal", "Meeting")
                val rows = mutableListOf<MutableList<String>>()
                var currentRow = mutableListOf<String>()
                
                categories.forEach { category ->
                    // Add to current row if it has less than 3 items, otherwise start a new row
                    if (currentRow.size < 3) {
                        currentRow.add(category)
                    } else {
                        rows.add(currentRow)
                        currentRow = mutableListOf()
                        currentRow.add(category)
                    }
                }
                // Add the last row if it has items
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                }
                
                // Display each row
                rows.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCategories.forEach { category ->
                            FilterChip(
                                selected = category == selectedCategory,
                                onClick = {
                                    selectedCategory = category
                                    if (category.isNotBlank()) categoryError = false
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getUserThemeColor(LocalContext.current), // User's theme color
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    // Add spacing between rows
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            if (categoryError) {
                Text(
                    text = "Category is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            SectionHeader("Date")
            OutlinedTextField(
                value = selectedDate?.format(DateTimeFormatter.ofPattern("dd MMMM, EEEE")) ?: "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = LocalDate.of(year, month + 1, day)
                                if (selectedDate != null) dateError = false
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                enabled = false,
                readOnly = true,
                placeholder = { Text("Select a date") },
                trailingIcon = { Icon(Icons.Default.CalendarToday, "Calendar") },
                isError = dateError,
                supportingText = {
                    if (dateError) Text("Date is required", color = MaterialTheme.colorScheme.error)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Add focused colors to match the user's theme color
                    focusedBorderColor = getUserThemeColor(LocalContext.current), // User's theme color
                    focusedLeadingIconColor = getUserThemeColor(LocalContext.current), // User's theme color
                    focusedTrailingIconColor = getUserThemeColor(LocalContext.current), // User's theme color
                    focusedLabelColor = getUserThemeColor(LocalContext.current) // User's theme color
                )
            )

            SectionHeader("Duration")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = hasDuration,
                    onCheckedChange = { hasDuration = it }
                )
                Spacer(Modifier.width(8.dp))
                Text("This task has a specific duration")
            }

            if (hasDuration) {
                // Show start and end time pickers when duration is enabled
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Start Time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        TimePickerField(
                            time = startTime,
                            onTimeSelected = { startTime = it },
                            placeholder = "Required"
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("End Time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        TimePickerField(
                            time = endTime,
                            onTimeSelected = {
                                endTime = it
                                timeError = false
                            },
                            placeholder = "Required",
                            isError = timeError,
                            errorMessage = "End time required"
                        )
                    }
                }
            } else {
                // Show single time picker when duration is disabled
                SectionHeader("Time")
                TimePickerField(
                    time = endTime,
                    onTimeSelected = {
                        endTime = it
                        timeError = false
                    },
                    placeholder = "Required",
                    isError = timeError,
                    errorMessage = "Time required"
                )
            }

            SectionHeader("Description")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Add more details...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = getUserThemeColor(LocalContext.current), // User's theme color
                    focusedLabelColor = getUserThemeColor(LocalContext.current) // User's theme color
                )
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    taskNameError = taskName.isBlank()
                    categoryError = selectedCategory.isBlank()
                    dateError = selectedDate == null
                    
                    // Validate time fields based on duration setting
                    if (hasDuration) {
                        timeError = startTime == null || endTime == null
                    } else {
                        timeError = endTime == null
                    }

                    if (taskNameError || categoryError || dateError || timeError) return@Button

                    // Format time display based on duration setting
                    val timeRange = if (hasDuration && startTime != null && endTime != null) {
                        "${startTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${endTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                    } else {
                        endTime!!.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    }

                    // Determine which time to use for notification (start time if duration, end time if not)
                    val notificationTime = if (hasDuration && startTime != null) {
                        startTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))
                    } else {
                        endTime!!.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }

                    val newTask = Task(
                        title = taskName,
                        time = timeRange,
                        category = selectedCategory,
                        deadlineDate = selectedDate!!.toString(),
                        deadlineTime = endTime!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                        description = description,
                        startTime = if (hasDuration) startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) else null,
                        notificationId = System.currentTimeMillis().toInt()
                    )
                    onTaskCreated(newTask)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = getUserThemeColor(LocalContext.current))
            ) {
                Text("Create Task", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun TimePickerField(
    time: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    Column {
        OutlinedTextField(
            value = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true // is24HourFormat
                    ).show()
                },
            enabled = false,
            readOnly = true,
            placeholder = { Text(placeholder) },
            trailingIcon = { Icon(Icons.Default.Schedule, "Time") },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.Transparent,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                // Add focused colors to match the user's theme color
                focusedBorderColor = getUserThemeColor(LocalContext.current), // User's theme color
                focusedLeadingIconColor = getUserThemeColor(LocalContext.current), // User's theme color
                focusedTrailingIconColor = getUserThemeColor(LocalContext.current), // User's theme color
                focusedLabelColor = getUserThemeColor(LocalContext.current) // User's theme color
            ),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left),
            isError = isError,
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun clickableTextFieldColors() = OutlinedTextFieldDefaults.colors(
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = Color.Transparent,
    disabledBorderColor = MaterialTheme.colorScheme.outline,
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    // Add focused colors to match the user's theme color
    focusedBorderColor = getUserThemeColor(LocalContext.current), // User's theme color
    focusedLeadingIconColor = getUserThemeColor(LocalContext.current), // User's theme color
    focusedTrailingIconColor = getUserThemeColor(LocalContext.current), // User's theme color
    focusedLabelColor = getUserThemeColor(LocalContext.current) // User's theme color
)

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}