package com.example.listify

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Data class with notification support
data class Task(
    val title: String,
    val time: String,
    val category: String = "Work",
    val deadlineDate: String = "2023-06-15",
    val deadlineTime: String = "14:30",
    val description: String = "Complete the project requirements and submit for review.",
    val isCompleted: Boolean = false,
    val startTime: String? = null,  // Optional start time (for duration-based tasks)
    val notificationId: Int = 0  // Unique ID for notification
)

// Primary accent color (defined once)
val primaryAccentColor = Color(0xFFF48FB1) // Light pink - will be overridden by user's theme color



// ✨ NEW: A custom slide-in drawer composable
@Composable
fun SideDrawer(
    isOpen: Boolean,
    navController: NavController,
    onClose: () -> Unit
) {
    // Animate the drawer's horizontal offset
    val drawerWidth = 240.dp
    val drawerOffset by animateDpAsState(
        targetValue = if (isOpen) 0.dp else -drawerWidth,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "drawerOffset"
    )

    // A semi-transparent scrim that covers the main content when the drawer is open
    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClose() }
                    )
                }
        )
    }

    // The drawer itself
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .offset(x = drawerOffset),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 48.dp) // Add extra top padding to avoid notch/status bar
        ) {
            val context = LocalContext.current
            val userProfile = context.getUserProfile() ?: UserProfile()
            
            // Header Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Info (Left Side)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Using user's avatar or a colored box as a placeholder
                    if (userProfile.avatarUri != null) {
                        AsyncImage(
                            model = userProfile.avatarUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Default colored box if no avatar is set
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(userProfile.themeColor)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f) // This will prevent text from colliding with the close button
                    ) {
                        Text(
                            userProfile.name.ifEmpty { "Sophia Rose" },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            userProfile.occupation.ifEmpty { "UX/UI Designer" },
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Spacer and Close Button (Right Side)
                Spacer(Modifier.weight(0.1f)) // Reduced weight to prevent excessive spacing
                IconButton(onClick = { onClose() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            // Separator
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            
            // Navigation Items
            val navItems = listOf(
                "Home" to Icons.Default.Home,
                "Exercise" to Icons.Default.FitnessCenter,
                "Settings" to Icons.Default.Settings
            )
            
            navItems.forEach { item ->
                NavigationDrawerItem(
                    text = item.first,
                    icon = item.second,
                    onClick = {
                        when (item.first) {
                            "Home" -> onClose()
                            "Exercise" -> {
                                navController.navigate("exercise")
                                onClose()
                            }
                            "Settings" -> {
                                // TODO: Implement settings navigation
                                onClose()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NavigationDrawerItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = text,
            tint = Color.Gray
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ProgressSummaryCard(tasks: List<Task>) {
    val context = LocalContext.current
    val userThemeColor = getUserThemeColor(context)
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(userThemeColor.copy(alpha = 0.2f), userThemeColor)
                )
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Today's progress summary", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$totalTasks Tasks", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Show completed tasks count
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Completed tasks",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$completedTasks completed", 
                        color = Color.White, 
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Progress", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        // ✨ FIX: progress parameter expects a Float, not a lambda
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        trackColor = Color.White.copy(alpha = 0.3f),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ... (getIconForCategory, ExpandedDetailRow, TaskItem, parseDate, parseTime are unchanged)
@Composable
fun getIconForCategory(category: String): ImageVector {
    return when (category.lowercase()) {
        "meeting" -> Icons.Filled.People
        "jira" -> Icons.Filled.Description
        "lunch" -> Icons.Filled.Fastfood // A better icon for lunch
        "Sync-Up" -> Icons.Filled.People // Corrected casing
        "personal" -> Icons.Filled.Person
        else -> Icons.Filled.Description
    }
}

@Composable
fun ExpandedDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(task: Task, onDelete: (Task) -> Unit, onComplete: (Task) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    if (!task.isCompleted) {
                        onComplete(task)
                    }
                }
                SwipeToDismissBoxValue.StartToEnd -> onDelete(task)
                SwipeToDismissBoxValue.Settled -> {}
            }
            return@rememberSwipeToDismissBoxState false
        },
        positionalThreshold = { it * 0.25f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = !task.isCompleted, // Only allow completing if not already completed
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
                    SwipeToDismissBoxValue.EndToStart -> if (task.isCompleted) Color.Gray.copy(alpha = 0.5f) else Color.Green.copy(alpha = 0.8f)
                    else -> Color.LightGray.copy(alpha = 0.2f)
                }, label = "background color"
            )
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                label = "icon scale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                Icon(
                    when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                        SwipeToDismissBoxValue.EndToStart -> if (task.isCompleted) Icons.Default.Lock else Icons.Default.Check
                        else -> Icons.Default.MoreHoriz
                    },
                    contentDescription = null,
                    modifier = Modifier.scale(scale),
                    tint = Color.White
                )
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (task.isCompleted) Color(0xFFE8F5E9) else Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp, 
                    if (task.isCompleted) Color(0xFF4CAF50) else Color(0xFFEEEEEE), 
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (task.isCompleted) Color(0xFF4CAF50).copy(alpha = 0.2f) 
                                else getUserThemeColor(LocalContext.current).copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            getIconForCategory(task.category),
                            contentDescription = task.category,
                            tint = if (task.isCompleted) Color(0xFF4CAF50) else getUserThemeColor(LocalContext.current),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (task.isCompleted) Color(0xFF4CAF50) else Color.Black,
                            style = TextStyle(
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.time,
                            color = if (task.isCompleted) Color(0xFF4CAF50).copy(alpha = 0.7f) else Color.Gray,
                            fontSize = 14.sp,
                            style = TextStyle(
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                        // Add a hint for completed tasks that they can still be deleted
                        if (task.isCompleted) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.SwipeRight,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50).copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Swipe right to delete",
                                    color = Color(0xFF4CAF50).copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Toggle expansion",
                            tint = Color.Gray
                        )
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show a completion indicator for completed tasks
                    if (task.isCompleted) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Completed",
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    ExpandedDetailRow(Icons.Filled.CalendarToday, "Deadline", "${task.deadlineDate} at ${task.deadlineTime}")
                    ExpandedDetailRow(Icons.Filled.Schedule, "Duration", task.time)
                    ExpandedDetailRow(Icons.Filled.Description, "Description", task.description)
                }
            }
        }
    }
}

fun parseDate(dateString: String): LocalDate { /* ... unchanged ... */
    return try {
        LocalDate.parse(dateString)
    } catch (e: Exception) {
        LocalDate.MAX
    }
}
fun parseTime(timeString: String): LocalTime { /* ... unchanged ... */
    return try {
        if (timeString.contains(":") && (timeString.contains("AM") || timeString.contains("PM"))) {
            LocalTime.parse(timeString, DateTimeFormatter.ofPattern("hh:mm a"))
        } else if (timeString.contains(":")) {
            LocalTime.parse(timeString)
        } else {
            LocalTime.MAX
        }
    } catch (e: Exception) {
        LocalTime.MAX
    }
}



@Composable
fun SwipeTutorialOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Swipe to Manage Tasks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Swipe right to delete a task",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Demonstration of swipe right (delete)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Swipe left to complete a task",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Demonstration of swipe left (complete)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.Green.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = getUserThemeColor(LocalContext.current))
                ) {
                    Text("Got it!", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageScreen(navController: NavController, tasks: List<Task>, onTaskDeleted: (Task) -> Unit, onCompleteTask: (Task) -> Unit) {
    val context = LocalContext.current
    val userThemeColor = getUserThemeColor(context)
    // ✨ CHANGE: State to control our custom drawer
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSwipeTutorial by remember { mutableStateOf(context.isFirstTimeWithSwipe()) }

    // ✨ CHANGE: The whole screen is wrapped in a Box to allow the drawer and tutorial to overlay it
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF9F9F9),
            topBar = {
                TopAppBar(
                    title = { Text("Homepage", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        // ✨ CHANGE: This button now opens our custom drawer
                        IconButton(onClick = { isDrawerOpen = true }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("notifications") }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("create-task") },
                    containerColor = userThemeColor,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    ProgressSummaryCard(tasks)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("Today's Task", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("See All", color = userThemeColor, fontWeight = FontWeight.Medium)
                    }
                }
                val sortedTasks = tasks.sortedWith(
                    compareBy({ it.isCompleted }, { parseDate(it.deadlineDate) }, { parseTime(it.deadlineTime) })
                )
                items(sortedTasks, key = { it.title + it.deadlineDate }) { task ->
                    TaskItem(task = task, onDelete = onTaskDeleted, onComplete = onCompleteTask)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // ✨ CHANGE: Our custom drawer is placed here, on top of the Scaffold
        SideDrawer(
            isOpen = isDrawerOpen,
            navController = navController,
            onClose = { isDrawerOpen = false }
        )
        
        // Show swipe tutorial for first-time users
        if (showSwipeTutorial) {
            SwipeTutorialOverlay(
                onDismiss = {
                    showSwipeTutorial = false
                    context.setFirstTimeWithSwipe(false)
                }
            )
        }
    }
}


