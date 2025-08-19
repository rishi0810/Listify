package com.example.listify

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter




data class NotificationRecord(
    val id: Int,
    val taskId: Int,
    val taskTitle: String,
    val taskTime: String,
    val triggeredAt: String, 
    val isCleared: Boolean = false
) {
    
    fun getTriggeredAt(): LocalDateTime {
        return try {
            LocalDateTime.parse(triggeredAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
    
    
    companion object {
        fun create(
            id: Int,
            taskId: Int,
            taskTitle: String,
            taskTime: String,
            triggeredAt: LocalDateTime,
            isCleared: Boolean = false
        ): NotificationRecord {
            return NotificationRecord(
                id = id,
                taskId = taskId,
                taskTitle = taskTitle,
                taskTime = taskTime,
                triggeredAt = triggeredAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                isCleared = isCleared
            )
        }
    }
}

class NotificationManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "notification_records"

    fun getNotifications(): MutableList<NotificationRecord> {
        val jsonString = sharedPreferences.getString(key, "[]")
        val type = object : TypeToken<MutableList<NotificationRecord>>() {}.type
        return try {
            gson.fromJson(jsonString, type) ?: mutableListOf()
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Error parsing notifications", e)
            mutableListOf()
        }
    }

    fun addNotification(notification: NotificationRecord) {
        val notifications = getNotifications()
        notifications.add(0, notification) 
        saveNotifications(notifications)
    }

    fun clearNotification(id: Int) {
        val notifications = getNotifications()
        val index = notifications.indexOfFirst { it.id == id }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isCleared = true)
            saveNotifications(notifications)
        }
    }

    fun clearAllNotifications() {
        val notifications = getNotifications()
        for (i in notifications.indices) {
            if (!notifications[i].isCleared) {
                notifications[i] = notifications[i].copy(isCleared = true)
            }
        }
        saveNotifications(notifications)
    }

    private fun saveNotifications(notifications: List<NotificationRecord>) {
        try {
            val jsonString = gson.toJson(notifications)
            sharedPreferences.edit().putString(key, jsonString).apply()
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Error saving notifications", e)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(navController: NavController) {
    val context = LocalContext.current
    val notificationManager = remember { NotificationManager(context) }
    var notificationList by remember { mutableStateOf<List<NotificationRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    
    LaunchedEffect(Unit) {
        val loaded = try {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                notificationManager.getNotifications()
            }
        } catch (e: Exception) {
            emptyList<NotificationRecord>()
        }
        notificationList = loaded.filter { !it.isCleared }
        loading = false
    }

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (loading) {
                
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (notificationList.isEmpty()) {
                EmptyNotificationsView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(notificationList, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClear = { 
                                notificationManager.clearNotification(notification.id)
                                notificationList = notificationList.filter { it.id != notification.id }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(
                        onClick = {
                            notificationManager.clearAllNotifications()
                            notificationList = emptyList()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear All", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Notifications")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = "No notifications",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your task reminders will appear here",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun NotificationItem(
    notification: NotificationRecord,
    onClear: () -> Unit
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = "Notification",
                    tint = getUserThemeColor(LocalContext.current),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.taskTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.taskTime,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Triggered at ${notification.getTriggeredAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}",
                color = Color.Gray.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}