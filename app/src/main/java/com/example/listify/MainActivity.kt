package com.example.listify

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.app.AlarmManager
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.listify.ui.theme.ListifyTheme

class MainActivity : ComponentActivity() {
    private lateinit var taskManager: TaskManager
    
   
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
       
    }
    
   
    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
       
    }
    
   
    private val deleteTaskReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "DELETE_TASK") {
                val notificationId = intent.getIntExtra("TASK_NOTIFICATION_ID", 0)
                taskManager.removeTaskByNotificationId(notificationId)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskManager = TaskManager(this)
        
       
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
       
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
       
        requestExactAlarmPermission()
        
       
        val filter = IntentFilter("DELETE_TASK")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(deleteTaskReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(deleteTaskReceiver, filter)
        }
        
       
       
        setContent {
            ListifyTheme {
               
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                ) {
                    NavigationHost(taskManager)
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
       
        setStatusBarAppearance()
    }
    
    private fun setStatusBarAppearance() {
       
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
    
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                exactAlarmPermissionLauncher.launch(intent)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(deleteTaskReceiver)
    }
}

@ExperimentalAnimationApi
fun NavGraphBuilder.animatedComposable(
    route: String,
    content: @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route = route,
        enterTransition = {
            if (route == "create-task") {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            } else {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            }
        },
        exitTransition = {
            if (route == "create-task") {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        },
        popEnterTransition = {
            if (route == "create-task") {
                slideInVertically(
                    initialOffsetY = { fullHeight -> -fullHeight / 4 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            } else {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            }
        },
        popExitTransition = {
            if (route == "create-task") {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        },
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationHost(taskManager: TaskManager) {
    val navController = rememberNavController()
    val tasks = remember { taskManager.getTasks() }
    
    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        animatedComposable("welcome") {
            WelcomeScreen(navController = navController, onNavigateToHome = { navController.navigate("onboarding") })
        }
        animatedComposable("onboarding") {
            UserOnboardingScreen(navController = navController)
        }
        animatedComposable("home") {
            HomepageScreen(
                navController = navController,
                tasks = tasks,
                onTaskDeleted = { task -> taskManager.removeTask(tasks, task) },
                onCompleteTask = { task -> 
                   
                    val updatedTask = task.copy(isCompleted = true)
                    taskManager.updateTask(tasks, task, updatedTask)
                }
            )
        }
        animatedComposable("create-task") {
            CreateTaskScreen(
                navController = navController,
                onTaskCreated = { newTask -> taskManager.addTask(tasks, newTask) }
            )
        }
        animatedComposable("exercise") {
            ExerciseScreen()
        }
        animatedComposable("notifications") {
            NotificationCenterScreen(navController = navController)
        }
    }
}