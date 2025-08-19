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
                
                saveNotificationToCenter(context, intent)
            }
            ACTION_CANCEL_TASK -> {
                cancelTask(context, intent)
            }
        }
    }

    private fun showTaskNotification(context: Context, intent: Intent) {
        android.util.Log.d("TaskNotificationReceiver", "Received task deadline notification")
        
        
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: return
        val taskTime = intent.getStringExtra("TASK_TIME") ?: return
        val notificationId = intent.getIntExtra("TASK_NOTIFICATION_ID", 0)
        
        android.util.Log.d("TaskNotificationReceiver", "Task title: $taskTitle")
        android.util.Log.d("TaskNotificationReceiver", "Task time: $taskTime")
        android.util.Log.d("TaskNotificationReceiver", "Notification ID: $notificationId")
        
        
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
        
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle("Task Reminder")
            .setContentText("$taskTitle is due soon")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$taskTitle\nDue at $taskTime"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_delete, 
                "Cancel Task",
                cancelPendingIntent
            )
        
        
        with(NotificationManagerCompat.from(context)) {
            
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
            
            val taskTitle = intent.getStringExtra("TASK_TITLE") ?: return
            val taskTime = intent.getStringExtra("TASK_TIME") ?: return
            val notificationId = intent.getIntExtra("TASK_NOTIFICATION_ID", 0)
            
            
            val notificationRecord = NotificationRecord.create(
                id = System.currentTimeMillis().toInt(),
                taskId = notificationId,
                taskTitle = taskTitle,
                taskTime = taskTime,
                triggeredAt = java.time.LocalDateTime.now()
            )
            
            
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
        
        
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
        
        
        val deleteIntent = Intent("DELETE_TASK").apply {
            putExtra("TASK_NOTIFICATION_ID", notificationId)
        }
        context.sendBroadcast(deleteIntent)
        
        android.util.Log.d("TaskNotificationReceiver", "Sent DELETE_TASK broadcast")
    }
}