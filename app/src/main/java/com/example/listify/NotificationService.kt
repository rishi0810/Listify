package com.example.listify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationService(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "task_deadline_channel"
        private const val CHANNEL_NAME = "Task Deadlines"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming task deadlines"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(task: Task) {
        try {
            android.util.Log.d("NotificationService", "=== SCHEDULING NOTIFICATION ===")
            android.util.Log.d("NotificationService", "Task: ${task.title}")
            android.util.Log.d("NotificationService", "Task ID: ${task.notificationId}")
            
            // Check if we have permission to schedule exact alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    android.util.Log.w("NotificationService", "No permission to schedule exact alarms")
                    // TODO: Show a message to the user requesting the permission
                    return
                }
            }
            
            // Calculate notification time (10 minutes before deadline)
            val notificationTime = calculateNotificationTime(task)
            
            android.util.Log.d("NotificationService", "Calculated notification time: $notificationTime")
            
            // Check if notification time is in the past
            val currentTime = java.time.LocalDateTime.now()
            android.util.Log.d("NotificationService", "Current time: $currentTime")
            
            if (notificationTime.isBefore(currentTime)) {
                android.util.Log.w("NotificationService", "Notification time is in the past, checking if we should adjust")
                android.util.Log.w("NotificationService", "Notification time: $notificationTime, Current time: $currentTime")
                
                // For testing purposes, if this is our test task, schedule it anyway but soon
                if (task.title == "Test Task" && task.notificationId == 99999) {
                    android.util.Log.d("NotificationService", "Test task detected, scheduling for 5 seconds from now regardless")
                    val adjustedTime = currentTime.plusSeconds(5)
                    scheduleAlarm(task, adjustedTime)
                    return
                }
                
                // For real tasks, skip scheduling if in the past
                android.util.Log.w("NotificationService", "Skipping scheduling for past notification")
                return
            }
            
            // Schedule the alarm for the calculated time
            scheduleAlarm(task, notificationTime)
            
        } catch (e: Exception) {
            // Handle any scheduling errors silently
            android.util.Log.e("NotificationService", "Error scheduling notification", e)
            e.printStackTrace()
        }
    }
    
    private fun scheduleAlarm(task: Task, notificationTime: LocalDateTime) {
        try {
            // Create intent for the receiver
            val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
                action = "TASK_DEADLINE"
                putExtra("TASK_TITLE", task.title)
                putExtra("TASK_TIME", task.time)
                putExtra("TASK_NOTIFICATION_ID", task.notificationId)
            }

            // Create pending intent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm
            val triggerTime = notificationTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000
            val currentTimeMillis = System.currentTimeMillis()
            
            android.util.Log.d("NotificationService", "Current time (ms): $currentTimeMillis")
            android.util.Log.d("NotificationService", "Trigger time (ms): $triggerTime")
            android.util.Log.d("NotificationService", "Time until trigger (ms): ${triggerTime - currentTimeMillis}")
            android.util.Log.d("NotificationService", "Time until trigger (min): ${(triggerTime - currentTimeMillis) / 60000}")
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            
            android.util.Log.d("NotificationService", "Alarm scheduled successfully")
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Error scheduling alarm", e)
            e.printStackTrace()
        }
    }

    fun cancelNotification(notificationId: Int) {
        try {
            // Cancel the scheduled alarm
            val intent = Intent(context, TaskNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            
            // Cancel any existing notification
            notificationManager.cancel(notificationId)
        } catch (e: Exception) {
            // Handle any cancellation errors silently
            e.printStackTrace()
        }
    }

    private fun calculateNotificationTime(task: Task): LocalDateTime {
        return try {
            android.util.Log.d("NotificationService", "Calculating notification time for task: ${task.title}")
            android.util.Log.d("NotificationService", "Task deadline date: ${task.deadlineDate}")
            android.util.Log.d("NotificationService", "Task deadline time: ${task.deadlineTime}")
            android.util.Log.d("NotificationService", "Task start time: ${task.startTime}")
            
            // For testing purposes, if this is our test task, schedule it for 5 seconds from now
            if (task.title == "Test Task" && task.notificationId == 99999) {
                val testTime = LocalDateTime.now().plusSeconds(5)
                android.util.Log.d("NotificationService", "Test task detected, scheduling for: $testTime")
                return testTime
            }
            
            // Parse the deadline date
            val deadlineDate = LocalDate.parse(task.deadlineDate)
            
            // Use start time if available (indicating a duration-based task), otherwise use deadline time
            val timeString = if (!task.startTime.isNullOrBlank()) {
                task.startTime // For duration-based tasks, use start time for notification
            } else {
                task.deadlineTime // For simple tasks, use deadline time for notification
            }
            
            android.util.Log.d("NotificationService", "Using time string: $timeString")
            
            // Try to parse with different formats
            val deadlineTime = try {
                // Try HH:mm format first (24-hour)
                LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                try {
                    // Try hh:mm a format (12-hour with AM/PM)
                    LocalTime.parse(timeString, DateTimeFormatter.ofPattern("hh:mm a"))
                } catch (e2: Exception) {
                    // Fallback to simple parse
                    LocalTime.parse(timeString)
                }
            }
            
            // Create deadline datetime
            val deadlineDateTime = LocalDateTime.of(deadlineDate, deadlineTime)
            
            android.util.Log.d("NotificationService", "Deadline datetime: $deadlineDateTime")
            
            // Subtract 10 minutes for notification time
            val notificationTime = deadlineDateTime.minusMinutes(10)
            android.util.Log.d("NotificationService", "Notification time: $notificationTime")
            
            notificationTime
        } catch (e: Exception) {
            android.util.Log.e("NotificationService", "Error calculating notification time", e)
            // If parsing fails, set notification for 1 hour from now
            val fallbackTime = LocalDateTime.now().plusHours(1)
            android.util.Log.d("NotificationService", "Using fallback time: $fallbackTime")
            fallbackTime
        }
    }
}
