package com.example.listify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.content.pm.PackageManager
import java.time.LocalDateTime

class TaskNotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "task_deadline_channel"
        private const val ACTION_CANCEL_TASK = "CANCEL_TASK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("TaskNotificationReceiver", "onReceive called with action: ${intent.action}")
        
        when (intent.action) {
            "TASK_DEADLINE" -> {
                showTaskNotification(context, intent)
                // Save notification to notification center
                saveNotificationToCenter(context, intent)
            }
            ACTION_CANCEL_TASK -> {
                cancelTask(context, intent)
            }
        }
    }

    private fun showTaskNotification(context: Context, intent: Intent) {
        android.util.Log.d("TaskNotificationReceiver", "Received task deadline notification")
        
        // Get task details from intent
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: return
        val taskTime = intent.getStringExtra("TASK_TIME") ?: return
        val notificationId = intent.getIntExtra("TASK_NOTIFICATION_ID", 0)
        
        android.util.Log.d("TaskNotificationReceiver", "Task title: $taskTitle")
        android.util.Log.d("TaskNotificationReceiver", "Task time: $taskTime")
        android.util.Log.d("TaskNotificationReceiver", "Notification ID: $notificationId")
        
        // Create intent for cancel action
        val cancelIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
            action = ACTION_CANCEL_TASK
            putExtra("TASK_NOTIFICATION_ID", notificationId)
        }
        
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a standard icon for now
            .setContentTitle("Task Reminder")
            .setContentText("$taskTitle is due soon")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$taskTitle\nDue at $taskTime"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_delete, // Using a standard icon for delete
                "Cancel Task",
                cancelPendingIntent
            )
        
        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            // Check for permission first (Android 13+)
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU || 
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(notificationId, builder.build())
                android.util.Log.d("TaskNotificationReceiver", "Notification displayed")
            } else {
                android.util.Log.d("TaskNotificationReceiver", "No permission to show notification")
            }
        }
    }

    private fun saveNotificationToCenter(context: Context, intent: Intent) {
        try {
            // Get task details from intent
            val taskTitle = intent.getStringExtra("TASK_TITLE") ?: return
            val taskTime = intent.getStringExtra("TASK_TIME") ?: return
            val notificationId = intent.getIntExtra("TASK_NOTIFICATION_ID", 0)
            
            // Create notification record
            val notificationRecord = NotificationRecord.create(
                id = System.currentTimeMillis().toInt(),
                taskId = notificationId,
                taskTitle = taskTitle,
                taskTime = taskTime,
                triggeredAt = java.time.LocalDateTime.now()
            )
            
            // Save to notification center
            val notificationManager = NotificationManager(context)
            notificationManager.addNotification(notificationRecord)
            
            android.util.Log.d("TaskNotificationReceiver", "Notification saved to center")
        } catch (e: Exception) {
            android.util.Log.e("TaskNotificationReceiver", "Error saving notification to center", e)
        }
    }

    private fun cancelTask(context: Context, intent: Intent) {
        android.util.Log.d("TaskNotificationReceiver", "Received cancel task request")
        
        val notificationId = intent.getIntExtra("TASK_NOTIFICATION_ID", 0)
        android.util.Log.d("TaskNotificationReceiver", "Canceling task with notification ID: $notificationId")
        
        // Cancel the notification
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
        
        // Send broadcast to main app to handle task deletion
        val deleteIntent = Intent("DELETE_TASK").apply {
            putExtra("TASK_NOTIFICATION_ID", notificationId)
        }
        context.sendBroadcast(deleteIntent)
        
        android.util.Log.d("TaskNotificationReceiver", "Sent DELETE_TASK broadcast")
    }
}