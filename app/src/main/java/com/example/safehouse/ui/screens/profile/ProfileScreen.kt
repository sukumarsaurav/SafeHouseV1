package com.example.safehouse.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.R
import com.example.safehouse.data.local.DataStoreHelper
import com.example.safehouse.data.models.UserProfileData
import com.example.safehouse.data.models.UserProfileResponse
import com.example.safehouse.data.network.ApiClient
import com.example.safehouse.navigation.Screen
import com.example.safehouse.ui.components.BottomNavigation
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Switch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import com.example.safehouse.data.models.UserPreferences
import com.example.safehouse.data.models.UpdatePreferencesRequest
import com.example.safehouse.data.models.PreferencesResponse
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var userProfile by remember { mutableStateOf<UserProfileData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val dataStoreHelper = remember { DataStoreHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.userService.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                userProfile = response.body()?.data
                isLoading = false
            } else {
                errorMessage = "Failed to load profile"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.localizedMessage}"
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Profile Header
                userProfile?.let { profile ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Image
                        AsyncImage(
                            model = profile.profile_image_url,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            error = painterResource(id = R.drawable.ic_profile_placeholder),
                            fallback = painterResource(id = R.drawable.ic_profile_placeholder)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = profile.full_name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (profile.is_verified) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Settings Options
                Spacer(modifier = Modifier.height(24.dp))
                
                // Profile Menu Items
                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    onClick = { /* Navigate to edit profile */ }
                )

                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = { navController.navigate(Screen.ChangePassword.route) }
                )

                // Preferences Section
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Notification Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        userProfile?.let { profile ->
                            PreferenceToggle(
                                title = "Email Notifications",
                                checked = profile.receive_email_notifications,
                                onCheckedChange = { newValue ->
                                    coroutineScope.launch {
                                        updateUserPreferences(
                                            profile = profile,
                                            emailNotifications = newValue,
                                            smsNotifications = profile.receive_sms_notifications,
                                            marketingOptIn = profile.marketing_opt_in
                                        ) { success ->
                                            showSuccessMessage = success
                                            if (success) {
                                                userProfile = profile.copy(receive_email_notifications = newValue)
                                            }
                                        }
                                    }
                                }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            PreferenceToggle(
                                title = "SMS Notifications",
                                checked = profile.receive_sms_notifications,
                                onCheckedChange = { newValue ->
                                    coroutineScope.launch {
                                        updateUserPreferences(
                                            profile = profile,
                                            emailNotifications = profile.receive_email_notifications,
                                            smsNotifications = newValue,
                                            marketingOptIn = profile.marketing_opt_in
                                        ) { success ->
                                            showSuccessMessage = success
                                            if (success) {
                                                userProfile = profile.copy(receive_sms_notifications = newValue)
                                            }
                                        }
                                    }
                                }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            PreferenceToggle(
                                title = "Marketing Communications",
                                checked = profile.marketing_opt_in,
                                onCheckedChange = { newValue ->
                                    coroutineScope.launch {
                                        updateUserPreferences(
                                            profile = profile,
                                            emailNotifications = profile.receive_email_notifications,
                                            smsNotifications = profile.receive_sms_notifications,
                                            marketingOptIn = newValue
                                        ) { success ->
                                            showSuccessMessage = success
                                            if (success) {
                                                userProfile = profile.copy(marketing_opt_in = newValue)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                if (showSuccessMessage) {
                    LaunchedEffect(Unit) {
                        delay(2000)
                        showSuccessMessage = false
                    }
                    Snackbar(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Preferences updated successfully")
                    }
                }

                // Push logout button to bottom
                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            dataStoreHelper.clearAuthData()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun PreferenceToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// Update the updatePreferences function
private suspend fun updateUserPreferences(
    profile: UserProfileData,
    emailNotifications: Boolean,
    smsNotifications: Boolean,
    marketingOptIn: Boolean,
    onComplete: (Boolean) -> Unit
) {
    try {
        val request = UpdatePreferencesRequest(
            receiveEmailNotifications = emailNotifications,
            receiveSmsNotifications = smsNotifications,
            marketingOptIn = marketingOptIn
        )
        
        val response = ApiClient.userService.updatePreferences(request)
        if (response.isSuccessful) {
            onComplete(true)
        } else {
            onComplete(false)
        }
    } catch (e: Exception) {
        onComplete(false)
    }
}

// Data class for user profile
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String
) 