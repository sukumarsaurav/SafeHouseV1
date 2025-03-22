package com.example.safehouse.ui.screens.locker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.data.models.Reservation
import com.example.safehouse.data.network.ApiClient
import com.example.safehouse.navigation.Screen
import com.example.safehouse.ui.components.BottomNavigation
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveLockersScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activeReservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    // Fetch active reservations when screen loads
    LaunchedEffect(Unit) {
        try {
            // In a real app, this would be an API call
            // val response = ApiClient.lockerService.getActiveReservations()
            
            // For now, we'll use dummy data
            delay(1000) // Simulate network delay
            activeReservations = getDummyReservations()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load active reservations: ${e.message}"
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Lockers") },
                actions = {
                    // Add history button
                    IconButton(
                        onClick = { navController.navigate(Screen.ReservationHistory.route) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Reservation History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigation(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (activeReservations.isEmpty()) {
                // Show empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No active lockers",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You don't have any active locker reservations",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { navController.navigate(Screen.LockerLocations.route) }
                    ) {
                        Text("Find a Locker")
                    }
                }
            } else {
                // Show list of active reservations
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeReservations) { reservation ->
                        ReservationCard(reservation = reservation, navController = navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationCard(reservation: Reservation, navController: NavController) {
    Card(
        onClick = {
            navController.navigate(Screen.ReservationDetails.route.replace("{reservationId}", reservation.id))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Locker #${reservation.lockerNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reservation.locationName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time remaining section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                
                val timeRemaining = calculateTimeRemaining(reservation.endTime)
                val hours = timeRemaining.first
                val minutes = timeRemaining.second
                
                Text(
                    text = when {
                        hours > 0L -> "$hours hours, $minutes minutes remaining"
                        minutes > 0L -> "$minutes minutes remaining"
                        else -> "Expiring soon"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (reservation.accessCode != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Access Code: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = reservation.accessCode,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Helper function to calculate time remaining
private fun calculateTimeRemaining(endTimeStr: String): Pair<Long, Long> {
    val endTime = try {
        Instant.parse(endTimeStr)
    } catch (e: Exception) {
        // Fallback if the format is different
        val formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault())
        Instant.from(formatter.parse(endTimeStr))
    }
    
    val now = Instant.now()
    val totalMinutes = ChronoUnit.MINUTES.between(now, endTime)
    
    if (totalMinutes <= 0) return Pair(0L, 0L)
    
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    
    return Pair(hours, minutes)
}

// Dummy data for testing
private fun getDummyReservations(): List<Reservation> {
    val now = Instant.now()
    
    return listOf(
        Reservation(
            id = "res1",
            lockerNumber = "A12",
            locationId = "loc1",
            locationName = "Downtown Center",
            startTime = now.minus(2, ChronoUnit.HOURS).toString(),
            endTime = now.plus(4, ChronoUnit.HOURS).toString(),
            status = "active",
            cost = 12.50,
            accessCode = "1234"
        ),
        Reservation(
            id = "res2",
            lockerNumber = "B05",
            locationId = "loc2",
            locationName = "Westside Mall",
            startTime = now.minus(1, ChronoUnit.DAYS).toString(),
            endTime = now.plus(1, ChronoUnit.DAYS).toString(),
            status = "active",
            cost = 24.00,
            accessCode = "5678"
        ),
        Reservation(
            id = "res3",
            lockerNumber = "C18",
            locationId = "loc3",
            locationName = "Eastside Plaza",
            startTime = now.minus(3, ChronoUnit.HOURS).toString(),
            endTime = now.plus(1, ChronoUnit.HOURS).toString(),
            status = "active",
            cost = 8.75,
            accessCode = "9012"
        )
    )
} 