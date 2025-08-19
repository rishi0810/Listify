package com.example.listify

import android.content.Context
import android.graphics.Bitmap
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

// Data class to hold user profile information
data class UserProfile(
    val name: String = "",
    val occupation: String = "",
    val avatarUri: String? = null,
    val themeColor: Color = Color(0xFFF48FB1) // Default light pink
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Load saved user profile if exists
    var userProfile by remember { 
        mutableStateOf(context.getUserProfile() ?: UserProfile()) 
    }
    
    var name by remember { mutableStateOf(userProfile.name) }
    var occupation by remember { mutableStateOf(userProfile.occupation) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedColor by remember { mutableStateOf(userProfile.themeColor) }
    
    // Launcher for image picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Persist read permission so URI remains accessible across restarts
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: Exception) {
                // ignore if permission not needed or fails
            }
            selectedImageUri = it
            // Decode off main thread and cache
            val uriString = it.toString()
            val cached = BitmapCache.get(uriString)
            if (cached != null) {
                bitmap = cached
            } else {
                // Launch a coroutine to decode and cache off main thread
                coroutineScope.launch {
                    val decoded = try {
                        withContext(Dispatchers.IO) {
                            uriToBitmap(context, it)
                        }
                    } catch (e: Exception) {
                        null
                    }
                    if (decoded != null) {
                        BitmapCache.put(uriString, decoded)
                        bitmap = decoded
                    }
                }
            }
        }
    }
    
    // Predefined color options
    val colorOptions = listOf(
        Color(0xFFF48FB1), // Light Pink (default)
        Color(0xFF64B5F6), // Light Blue
        Color(0xFF81C784), // Light Green
        Color(0xFFFFB74D), // Light Orange
        Color(0xFFBA68C8), // Light Purple
        Color(0xFFFF6B6B), // Light Red
        Color(0xFF4DB6AC)  // Light Teal
    )
    
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Profile Setup", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            )
        },
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
                            // Save user profile
                            val avatarString = selectedImageUri?.toString() ?: userProfile.avatarUri
                            val profile = UserProfile(
                                name = name,
                                occupation = occupation,
                                avatarUri = avatarString,
                                themeColor = selectedColor
                            )
                            context.saveUserProfile(profile)

                            // Mark onboarding as completed
                            context.setOnboardingCompleted(true)

                            // Navigate to home screen
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
            // Scrollable content
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

                // Avatar Selection
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

                // Name Input
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

                // Occupation Input
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

                // Theme Color Selection
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

// Helper function to convert URI to Bitmap
fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Extension functions for SharedPreferences
fun Context.getUserProfile(): UserProfile? {
    val sharedPref = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    val name = sharedPref.getString("name", null) ?: return null
    val occupation = sharedPref.getString("occupation", "") ?: ""
    val avatarUri = sharedPref.getString("avatar_uri", null)
    val themeColorHash = sharedPref.getInt("theme_color", Color(0xFFF48FB1).hashCode())
    
    // For simplicity, we'll just return the default color
    // In a real app, you might want to store the ARGB values separately
    val themeColor = when (themeColorHash) {
        Color(0xFFF48FB1).hashCode() -> Color(0xFFF48FB1) // Light Pink
        Color(0xFF64B5F6).hashCode() -> Color(0xFF64B5F6) // Light Blue
        Color(0xFF81C784).hashCode() -> Color(0xFF81C784) // Light Green
        Color(0xFFFFB74D).hashCode() -> Color(0xFFFFB74D) // Light Orange
        Color(0xFFBA68C8).hashCode() -> Color(0xFFBA68C8) // Light Purple
        Color(0xFFFF6B6B).hashCode() -> Color(0xFFFF6B6B) // Light Red
        Color(0xFF4DB6AC).hashCode() -> Color(0xFF4DB6AC) // Light Teal
        else -> Color(0xFFF48FB1) // Default to light pink
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
        putInt("theme_color", profile.themeColor.hashCode()) // Using hashCode instead of toArgb
        apply()
    }
}

// Helper function to get user's theme color
fun getUserThemeColor(context: Context): Color {
    val userProfile = context.getUserProfile()
    return userProfile?.themeColor ?: Color(0xFFF48FB1) // Default to light pink if not set
}

// Extension functions for onboarding completion tracking
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