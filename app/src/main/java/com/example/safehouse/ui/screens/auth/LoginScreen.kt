package com.example.safehouse.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.R
import com.example.safehouse.data.network.ApiClient
import com.example.safehouse.data.models.LoginRequest
import com.example.safehouse.data.local.DataStoreHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Create a sealed class for navigation routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Signup : Screen("signup")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStoreHelper = remember { DataStoreHelper(context) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Safe House Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 32.dp)
        )
        
        Text(
            text = "SafeHouse",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Secure Locker Rentals",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { 
                // Only allow digits in the phone field
                if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                    phoneNumber = it
                }
            },
            label = { Text("Phone Number") },
            placeholder = { Text("9991234567") }, // Changed to show format without country code
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = {
                // Format phone number with +91 prefix for India
                val formattedPhone = "+91${phoneNumber.trim()}"
                
                // Handle login logic
                coroutineScope.launch {
                    try {
                        isLoading = true
                        errorMessage = null
                        
                        val loginRequest = LoginRequest(phone = formattedPhone, password = password)
                        val response = ApiClient.authService.login(loginRequest)
                        
                        when {
                            response.isSuccessful -> {
                                response.body()?.let { authResponse ->
                                    if (authResponse.success) {
                                        // Create a coroutine scope for DataStore operations
                                        withContext(Dispatchers.IO) {
                                            // Save auth data to DataStore
                                            dataStoreHelper.saveAuthToken(authResponse.data.token)
                                            dataStoreHelper.saveUserId(authResponse.data.userId)
                                            dataStoreHelper.saveRefreshToken(authResponse.data.refreshToken)
                                        }
                                        
                                        // Navigate to home screen
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route)
                                            launchSingleTop = true
                                        }
                                    } else {
                                        errorMessage = authResponse.message ?: "Login failed"
                                    }
                                }
                            }
                            else -> {
                                errorMessage = "Login failed: ${response.errorBody()?.string() ?: "Invalid credentials"}"
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Network error: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading && phoneNumber.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
        
        TextButton(
            onClick = { navController.navigate(Screen.Signup.route) }
        ) {
            Text("Don't have an account? Sign Up")
        }
    }
} 