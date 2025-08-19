package com.example.listify

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.listify.UserProfile


@Composable
fun WelcomeScreen(navController: NavController, onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    var isFirstTime by remember { mutableStateOf<Boolean?>(null) }
    
    
    LaunchedEffect(Unit) {
        isFirstTime = context.isFirstTimeUser()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                
                Image(
                    painter = painterResource(id = com.example.listify.R.mipmap.welcome),
                    contentDescription = "Welcome Illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            
            Text(
                text = "Welcome to Listify",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            
            Text(
                text = "Something I created for you to track your endless JIRA tickets",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            
            when (isFirstTime) {
                true -> {
                    
                    Button(
                        onClick = {
                            context.setFirstTimeUser(false)
                            
                            if (context.isOnboardingCompleted()) {
                                
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            } else {
                                
                                onNavigateToHome()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1C1C1E) 
                        )
                    ) {
                        Text(
                            text = "Let's Start",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                false -> {
                    
                    CircularProgressIndicator(
                        modifier = Modifier.height(56.dp),
                        color = Color(0xFF1C1C1E)
                    )
                    
                    
                    LaunchedEffect(Unit) {
                        delay(3000) 
                        
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                }
                null -> {
                    
                    CircularProgressIndicator(
                        modifier = Modifier.height(56.dp),
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


fun Context.isFirstTimeUser(): Boolean {
    val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return sharedPref.getBoolean("is_first_time", true)
}

fun Context.setFirstTimeUser(isFirstTime: Boolean) {
    val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putBoolean("is_first_time", isFirstTime)
        apply()
    }
}


