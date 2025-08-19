package com.example.listify

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val notificationService = NotificationService(context)
    private var tasks: SnapshotStateList<Task>? = null
    
    fun getTasks(): SnapshotStateList<Task> {
        if (tasks == null) {
            tasks = mutableStateListOf<Task>()
            val jsonString = sharedPreferences.getString("tasks", "[]")
            val type = object : TypeToken<List<Task>>() {}.type
            try {
                val taskList: List<Task> = gson.fromJson(jsonString, type)
                tasks!!.addAll(taskList)
            } catch (e: Exception) {
                // Handle parsing errors
                e.printStackTrace()
            }
        }
        return tasks!!
    }
    
    fun saveTasks(tasks: List<Task>) {
        try {
            val jsonString = gson.toJson(tasks)
            sharedPreferences.edit().putString("tasks", jsonString).apply()
        } catch (e: Exception) {
            // Handle serialization errors
            e.printStackTrace()
        }
    }
    
    fun addTask(tasks: SnapshotStateList<Task>, task: Task) {
        android.util.Log.d("TaskManager", "Adding task: ${task.title}")
        android.util.Log.d("TaskManager", "Task notification ID: ${task.notificationId}")
        android.util.Log.d("TaskManager", "Task deadline date: ${task.deadlineDate}")
        android.util.Log.d("TaskManager", "Task deadline time: ${task.deadlineTime}")
        android.util.Log.d("TaskManager", "Task start time: ${task.startTime}")
        
        tasks.add(task)
        saveTasks(tasks)
        // Schedule notification for the new task
        try {
            notificationService.scheduleNotification(task)
        } catch (e: Exception) {
            // Handle notification scheduling errors
            android.util.Log.e("TaskManager", "Error scheduling notification", e)
            e.printStackTrace()
        }
    }
    
    fun removeTask(tasks: SnapshotStateList<Task>, task: Task) {
        // Cancel the notification before removing the task
        try {
            notificationService.cancelNotification(task.notificationId)
        } catch (e: Exception) {
            // Handle notification cancellation errors
            e.printStackTrace()
        }
        tasks.remove(task)
        saveTasks(tasks)
    }
    
    fun removeTaskByNotificationId(notificationId: Int) {
        try {
            val tasksList = getTasks()
            val taskToRemove = tasksList.find { it.notificationId == notificationId }
            if (taskToRemove != null) {
                removeTask(tasksList, taskToRemove)
            }
        } catch (e: Exception) {
            // Handle task removal errors
            e.printStackTrace()
        }
    }
    
    fun updateTask(tasks: SnapshotStateList<Task>, oldTask: Task, newTask: Task) {
        val index = tasks.indexOfFirst { 
            it.title == oldTask.title && it.deadlineDate == oldTask.deadlineDate 
        }
        if (index != -1) {
            // Cancel the old notification
            try {
                notificationService.cancelNotification(oldTask.notificationId)
            } catch (e: Exception) {
                // Handle notification cancellation errors
                e.printStackTrace()
            }
            tasks[index] = newTask
            saveTasks(tasks)
            // Schedule new notification
            try {
                notificationService.scheduleNotification(newTask)
            } catch (e: Exception) {
                // Handle notification scheduling errors
                e.printStackTrace()
            }
        }
    }
    
    fun clearAllTasks() {
        try {
            // Get all tasks to cancel their notifications
            val allTasks = getTasks()
            for (task in allTasks) {
                notificationService.cancelNotification(task.notificationId)
            }
            sharedPreferences.edit().putString("tasks", "[]").apply()
        } catch (e: Exception) {
            // Handle clear tasks errors
            e.printStackTrace()
        }
    }
}