package com.example.safehouse.ui.screens.locker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.data.models.LocationDetail
import com.example.safehouse.data.models.LockerDetail
import com.example.safehouse.data.models.Locker
import com.example.safehouse.data.models.LockerLocation
import com.example.safehouse.data.models.OpeningHours
import com.example.safehouse.navigation.Screen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockerDetailsScreen(locationId: String, navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    var location by remember { mutableStateOf<LockerLocation?>(null) }
    var lockers by remember { mutableStateOf<List<Locker>>(emptyList()) }
    var openingHours by remember { mutableStateOf<OpeningHours?>(null) }
    var selectedLocker by remember { mutableStateOf<Locker?>(null) }
    var showReservationDialog by remember { mutableStateOf(false) }
    var reservationDuration by remember { mutableStateOf(1) } // Default 1 hour
    
    // Simulate fetching location details
    LaunchedEffect(locationId) {
        delay(1000)
        
        location = LockerLocation(
            id = locationId,
            name = "Downtown Center",
            address = "123 Main St, Downtown",
            latitude = 37.7749,
            longitude = -122.4194,
            totalLockers = 20,
            availableLockers = 15,
            distance = 0.8
        )
        
        lockers = listOf(
            Locker(
                id = "lock1",
                number = "A01",
                size = "Small",
                status = "available",
                hourlyRate = 2.50
            ),
            Locker(
                id = "lock2",
                number = "A02",
                size = "Medium",
                status = "available",
                hourlyRate = 3.50
            ),
            Locker(
                id = "lock3",
                number = "A03",
                size = "Large",
                status = "occupied",
                hourlyRate = 5.00
            ),
            Locker(
                id = "lock4",
                number = "B01",
                size = "Small",
                status = "available",
                hourlyRate = 2.50
            ),
            Locker(
                id = "lock5",
                number = "B02",
                size = "Medium",
                status = "available",
                hourlyRate = 3.50
            )
        )
        
        openingHours = OpeningHours(
            monday = "08:00 - 20:00",
            tuesday = "08:00 - 20:00",
            wednesday = "08:00 - 20:00",
            thursday = "08:00 - 20:00",
            friday = "08:00 - 22:00",
            saturday = "09:00 - 22:00",
            sunday = "10:00 - 18:00"
        )
        
        isLoading = false
    }
    
    if (showReservationDialog && selectedLocker != null) {
        ReservationDialog(
            locker = selectedLocker!!,
            duration = reservationDuration,
            onDurationChange = { reservationDuration = it },
            onConfirm = {
                // Reserve the locker and navigate to home screen
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            onDismiss = { showReservationDialog = false }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(location?.name ?: "Locker Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Location Map
                item {
                    location?.let {
                        LocationMap(location = it)
                    }
                }
                
                // Location Details
                item {
                    location?.let {
                        LocationDetails(location = it, openingHours = openingHours)
                    }
                }
                
                // Available Lockers Section
                item {
                    Text(
                        text = "Available Lockers",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // Locker List
                items(lockers.filter { it.status == "available" }) { locker ->
                    LockerItem(locker = locker) {
                        selectedLocker = locker
                        showReservationDialog = true
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun LocationMap(location: LockerLocation) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(location.latitude, location.longitude), 15f
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                title = location.name
            )
        }
    }
}

@Composable
fun LocationDetails(location: LockerLocation, openingHours: OpeningHours?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = location.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = location.address,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Opening Hours",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        openingHours?.let {
            OpeningHoursRow("Monday", it.monday)
            OpeningHoursRow("Tuesday", it.tuesday)
            OpeningHoursRow("Wednesday", it.wednesday)
            OpeningHoursRow("Thursday", it.thursday)
            OpeningHoursRow("Friday", it.friday)
            OpeningHoursRow("Saturday", it.saturday)
            OpeningHoursRow("Sunday", it.sunday)
        }
    }
}

@Composable
fun OpeningHoursRow(day: String, hours: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = hours,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LockerItem(locker: Locker, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Locker #${locker.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = locker.size,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", locker.hourlyRate)}/hr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun ReservationDialog(
    locker: Locker,
    duration: Int,
    onDurationChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reserve Locker #${locker.number}") },
        text = {
            Column {
                Text("Size: ${locker.size}")
                Text("Rate: $${String.format("%.2f", locker.hourlyRate)}/hour")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select duration:")
                
                Slider(
                    value = duration.toFloat(),
                    onValueChange = { onDurationChange(it.toInt()) },
                    valueRange = 1f..24f,
                    steps = 22
                )
                
                Text("$duration hour${if (duration > 1) "s" else ""}")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Total: $${String.format("%.2f", locker.hourlyRate * duration)}",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Reserve")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 