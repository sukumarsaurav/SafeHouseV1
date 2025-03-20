package com.example.safehouse.ui.screens.locker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.safehouse.data.models.Reservation
import com.example.safehouse.navigation.Screen
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailsScreen(reservationId: String, navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var showExtendDialog by remember { mutableStateOf(false) }
    var extendHours by remember { mutableStateOf(1) }
    var showReleaseConfirmDialog by remember { mutableStateOf(false) }
    
    // Simulate fetching reservation details
    LaunchedEffect(reservationId) {
        delay(1000)
        reservation = Reservation(
            id = reservationId,
            lockerNumber = "A101",
            locationId = "loc-123",
            locationName = "Downtown Center",
            startTime = "2023-06-15T10:00:00Z",
            endTime = "2023-06-15T14:00:00Z",
            status = "active",
            cost = 12.50,
            accessCode = "1234"
        )
        isLoading = false
    }
    
    if (showExtendDialog) {
        ExtendReservationDialog(
            currentEndTime = formatDateTime(reservation?.endTime ?: ""),
            hours = extendHours,
            onHoursChange = { extendHours = it },
            onConfirm = {
                // Handle reservation extension
                showExtendDialog = false
            },
            onDismiss = { showExtendDialog = false }
        )
    }
    
    if (showReleaseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showReleaseConfirmDialog = false },
            title = { Text("Release Locker") },
            text = { Text("Are you sure you want to end your reservation? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle locker release
                        showReleaseConfirmDialog = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Release")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReleaseConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservation Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                reservation?.let { res ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Main Reservation Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = res.locationName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Locker #${res.lockerNumber}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Access Code Display
                                res.accessCode?.let { code ->
                                    Text(
                                        text = "Access Code",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = code,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 8.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Time Information
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Time"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Start: ${formatDateTime(res.startTime)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "End: ${formatDateTime(res.endTime)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Location
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = res.locationName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Status Chip
                                Surface(
                                    color = when (res.status) {
                                        "active" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.error
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = res.status.capitalize(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showExtendDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Extend Time")
                            }
                            
                            Button(
                                onClick = { showReleaseConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Release Locker")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExtendReservationDialog(
    currentEndTime: String,
    hours: Int,
    onHoursChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Extend Reservation") },
        text = {
            Column {
                Text("Current end time: $currentEndTime")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select additional hours:")
                
                Slider(
                    value = hours.toFloat(),
                    onValueChange = { onHoursChange(it.toInt()) },
                    valueRange = 1f..12f,
                    steps = 10
                )
                
                Text("$hours hour${if (hours > 1) "s" else ""}")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // In a real app, calculate the new end time and cost here
                Text(
                    text = "Additional cost: $${String.format("%.2f", hours * 3.50)}",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Extend")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to format date-time strings
private fun formatDateTime(dateTimeString: String): String {
    return try {
        val instant = Instant.parse(dateTimeString)
        val formatter = DateTimeFormatter
            .ofPattern("MMM dd, yyyy hh:mm a")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateTimeString // Fallback to original string if parsing fails
    }
}

// Extension function to capitalize strings
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
} 