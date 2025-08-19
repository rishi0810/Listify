package com.example.listify

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class UserProfile(
    val name: String = "",
    val occupation: String = "",
    val avatarUri: String? = null,
    val themeColor: Color = Color.Black
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    
    var userProfile by remember { 
        mutableStateOf(context.getUserProfile() ?: UserProfile()) 
    }
    
    var name by remember { mutableStateOf(userProfile.name) }
    var occupation by remember { mutableStateOf(userProfile.occupation) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedColor by remember { mutableStateOf(userProfile.themeColor) }
    
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
            uri?.let {
                try {
                    
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                } catch (e: Exception) {
                    
                }
                
                selectedImageUri = it
            }
    }
    
    
    val colorOptions = listOf(
        Color(0xFFF48FB1), 
        Color(0xFF64B5F6), 
        Color(0xFF81C784), 
        Color(0xFFFFB74D), 
        Color(0xFFBA68C8), 
        Color(0xFFFF6B6B), 
        Color(0xFF4DB6AC)  
    )
    
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            
                            val avatarString = selectedImageUri?.toString() ?: userProfile.avatarUri
                            val profile = UserProfile(
                                name = name,
                                occupation = occupation,
                                avatarUri = avatarString,
                                themeColor = selectedColor
                            )
                            context.saveUserProfile(profile)

                            
                            context.setOnboardingCompleted(true)

                            
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
                        enabled = name.isNotBlank()
                    ) {
                        Text(
                            "Continue",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Let's personalize your experience",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                
                SectionHeader("Profile Picture")
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        userProfile.avatarUri != null -> {
                            AsyncImage(
                                model = userProfile.avatarUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Add Photo",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }

                Text(
                    text = "Tap to select a photo",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                
                SectionHeader("Your Name")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your name") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Name",
                            tint = selectedColor
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        focusedLabelColor = selectedColor
                    )
                )

                
                SectionHeader("Occupation")
                OutlinedTextField(
                    value = occupation,
                    onValueChange = { occupation = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What's your job title?") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = "Occupation",
                            tint = selectedColor
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        focusedLabelColor = selectedColor
                    )
                )

                
                SectionHeader("Theme Color")
                Text(
                    text = "Choose your favorite color",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(bottom = 16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions.size) { index ->
                        val color = colorOptions[index]
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (color == selectedColor) 4.dp else 0.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .border(
                                    2.dp,
                                    Color.Gray.copy(alpha = 0.5f),
                                    CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}





fun Context.getUserProfile(): UserProfile? {
    val sharedPref = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    val name = sharedPref.getString("name", null) ?: return null
    val occupation = sharedPref.getString("occupation", "") ?: ""
    val avatarUri = sharedPref.getString("avatar_uri", null)
    val themeColorHash = sharedPref.getInt("theme_color", Color(0xFFF48FB1).hashCode())
    
    
    
    val themeColor = when (themeColorHash) {
        Color(0xFFF48FB1).hashCode() -> Color(0xFFF48FB1) 
        Color(0xFF64B5F6).hashCode() -> Color(0xFF64B5F6) 
        Color(0xFF81C784).hashCode() -> Color(0xFF81C784) 
        Color(0xFFFFB74D).hashCode() -> Color(0xFFFFB74D) 
        Color(0xFFBA68C8).hashCode() -> Color(0xFFBA68C8) 
        Color(0xFFFF6B6B).hashCode() -> Color(0xFFFF6B6B) 
        Color(0xFF4DB6AC).hashCode() -> Color(0xFF4DB6AC) 
        else -> Color(0xFFF48FB1) 
    }
    
    return UserProfile(
        name = name,
        occupation = occupation,
        avatarUri = avatarUri,
        themeColor = themeColor
    )
}

fun Context.saveUserProfile(profile: UserProfile) {
    val sharedPref = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("name", profile.name)
        putString("occupation", profile.occupation)
        putString("avatar_uri", profile.avatarUri)
        putInt("theme_color", profile.themeColor.hashCode()) 
        apply()
    }
}


fun getUserThemeColor(context: Context): Color {
    val userProfile = context.getUserProfile()
    return userProfile?.themeColor ?: Color(0xFFF48FB1) 
}


fun Context.isOnboardingCompleted(): Boolean {
    val sharedPref = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
    return sharedPref.getBoolean("is_onboarding_completed", false)
}

fun Context.setOnboardingCompleted(completed: Boolean) {
    val sharedPref = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putBoolean("is_onboarding_completed", completed)
        apply()
    }
}