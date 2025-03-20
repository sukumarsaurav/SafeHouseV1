package com.example.safehouse.ui.screens.locker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.data.models.LockerLocation
import com.example.safehouse.navigation.Screen
import com.example.safehouse.ui.components.BottomNavigation
import com.example.safehouse.ui.screens.home.NearbyLocationCard
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockerLocationsScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    var showMap by remember { mutableStateOf(false) }
    var locations by remember { mutableStateOf<List<LockerLocation>>(emptyList()) }
    
    // Simulate loading locations
    LaunchedEffect(Unit) {
        delay(1000)
        locations = listOf(
            LockerLocation(
                id = "loc1",
                name = "Downtown Center",
                address = "123 Main St, Downtown",
                latitude = 37.7749,
                longitude = -122.4194,
                totalLockers = 20,
                availableLockers = 15,
                distance = 0.8
            ),
            LockerLocation(
                id = "loc2",
                name = "Westside Mall",
                address = "456 West Ave, Westside",
                latitude = 37.7739,
                longitude = -122.4312,
                totalLockers = 15,
                availableLockers = 8,
                distance = 2.3
            ),
            LockerLocation(
                id = "loc3",
                name = "Eastside Plaza",
                address = "789 East Blvd, Eastside",
                latitude = 37.7831,
                longitude = -122.4039,
                totalLockers = 25,
                availableLockers = 12,
                distance = 3.1
            ),
            LockerLocation(
                id = "loc4",
                name = "North Station",
                address = "101 North St, Northside",
                latitude = 37.7937,
                longitude = -122.4048,
                totalLockers = 18,
                availableLockers = 5,
                distance = 4.5
            ),
            LockerLocation(
                id = "loc5",
                name = "South Terminal",
                address = "202 South Ave, Southside",
                latitude = 37.7649,
                longitude = -122.4194,
                totalLockers = 30,
                availableLockers = 22,
                distance = 5.2
            )
        )
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locker Locations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMap = !showMap }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Filter"
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
            } else if (showMap) {
                LocationsMapView(locations, navController)
            } else {
                LocationsListView(locations, navController)
            }
        }
    }
}

@Composable
fun LocationsListView(locations: List<LockerLocation>, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(locations) { location ->
            NearbyLocationCard(
                locationName = location.name,
                address = location.address,
                availableLockers = location.availableLockers,
                distance = location.distance ?: 0.0,
                onClick = {
                    navController.navigate(Screen.LockerDetails.route.replace("{locationId}", location.id))
                }
            )
        }
    }
}

@Composable
fun LocationsMapView(locations: List<LockerLocation>, navController: NavController) {
    if (locations.isEmpty()) return
    
    // Calculate center position by averaging all location coordinates
    val centerLat = locations.map { it.latitude }.average()
    val centerLng = locations.map { it.longitude }.average()
    val center = LatLng(centerLat, centerLng)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 12f)
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        locations.forEach { location ->
            Marker(
                state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                title = location.name,
                snippet = "${location.availableLockers} lockers available",
                onClick = {
                    navController.navigate(Screen.LockerDetails.route.replace("{locationId}", location.id))
                    true
                }
            )
        }
    }
} 