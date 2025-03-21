package com.example.safehouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.safehouse.data.network.ApiClient
import com.example.safehouse.navigation.SafeHouseNavigation
import com.example.safehouse.ui.theme.SafeHouseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ApiClient with context
        ApiClient.initialize(applicationContext)
        
        setContent {
            SafeHouseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SafeHouseNavigation(navController = navController)
                }
            }
        }
    }
}