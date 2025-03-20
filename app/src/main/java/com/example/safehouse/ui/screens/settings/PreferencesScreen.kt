package com.example.safehouse.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    
    // Notification preferences
    var enablePushNotifications by remember { mutableStateOf(true) }
    var enableReservationReminders by remember { mutableStateOf(true) }
    var enablePromoNotifications by remember { mutableStateOf(false) }
    
    // Location preferences
    var enableLocationServices by remember { mutableStateOf(true) }
    var defaultSearchRadius by remember { mutableStateOf(5) } // km
    
    // Payment preferences
    var enableAutoPayment by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Notifications section
            PreferenceSection(title = "Notifications") {
                SwitchPreference(
                    title = "Push Notifications",
                    description = "Receive push notifications",
                    checked = enablePushNotifications,
                    onCheckedChange = { enablePushNotifications = it }
                )
                
                Divider()
                
                SwitchPreference(
                    title = "Reservation Reminders",
                    description = "Get reminded before your reservation ends",
                    checked = enableReservationReminders,
                    onCheckedChange = { enableReservationReminders = it },
                    enabled = enablePushNotifications
                )
                
                Divider()
                
                SwitchPreference(
                    title = "Promotional Notifications",
                    description = "Receive special offers and promotions",
                    checked = enablePromoNotifications,
                    onCheckedChange = { enablePromoNotifications = it },
                    enabled = enablePushNotifications
                )
            }
            
            // Location preferences
            PreferenceSection(title = "Location") {
                SwitchPreference(
                    title = "Location Services",
                    description = "Allow app to access your location",
                    checked = enableLocationServices,
                    onCheckedChange = { enableLocationServices = it }
                )
                
                Divider()
                
                SliderPreference(
                    title = "Default Search Radius",
                    description = "Distance for nearby locker search",
                    value = defaultSearchRadius,
                    onValueChange = { defaultSearchRadius = it },
                    valueRange = 1f..20f,
                    steps = 18,
                    valueRepresentation = { "$it km" },
                    enabled = enableLocationServices
                )
            }
            
            // Payment preferences
            PreferenceSection(title = "Payment") {
                SwitchPreference(
                    title = "Auto Payment",
                    description = "Automatically use saved payment method",
                    checked = enableAutoPayment,
                    onCheckedChange = { enableAutoPayment = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    isLoading = true
                    
                    // Simulate saving preferences
                    scope.launch {
                        delay(1500)
                        isLoading = false
                        snackbarHostState.showSnackbar("Preferences saved")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Save Preferences")
                }
            }
        }
    }
    
    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Preferences updated successfully")
            showSuccessSnackbar = false
        }
    }
}

@Composable
fun PreferenceSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SliderPreference(
    title: String,
    description: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueRepresentation: (Int) -> String,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = valueRepresentation(value),
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
} 