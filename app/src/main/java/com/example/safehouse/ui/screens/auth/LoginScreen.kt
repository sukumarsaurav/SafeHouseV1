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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.R
import com.example.safehouse.navigation.Screen
import com.example.safehouse.data.network.ApiClient
import com.example.safehouse.data.models.LoginRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    
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
                        
                        if (response.isSuccessful && response.body() != null) {
                            // Store token for future API calls
                            val authResponse = response.body()!!
                            // Here you would typically save the token to preferences or a secure storage
                            
                            // Navigate to home screen
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            // Handle error
                            errorMessage = "Login failed: ${response.errorBody()?.string() ?: "Invalid credentials"}"
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Login")
            }
        }
        
        TextButton(
            onClick = {
                // Format phone number with +91 prefix for India
                val formattedPhone = "+91${phoneNumber.trim()}"
                navController.navigate(Screen.OtpVerification.route.replace("{phoneNumber}", formattedPhone))
            },
            modifier = Modifier.padding(top = 8.dp),
            enabled = phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }
        ) {
            Text("Login with OTP")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            TextButton(onClick = { navController.navigate(Screen.Signup.route) }) {
                Text("Sign Up")
            }
        }
    }
} 