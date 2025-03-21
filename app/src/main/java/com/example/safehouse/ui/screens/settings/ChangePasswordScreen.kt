package com.example.safehouse.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.data.local.DataStoreHelper
import com.example.safehouse.data.models.ChangePasswordRequest
import com.example.safehouse.data.models.MessageResponse
import com.example.safehouse.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStoreHelper = remember { DataStoreHelper(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { 
                    currentPassword = it
                    errorMessage = null
                },
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { currentPasswordVisible = !currentPasswordVisible }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = if (currentPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    errorMessage = null
                },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { newPasswordVisible = !newPasswordVisible }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Button(
                onClick = {
                    // Validate inputs
                    when {
                        currentPassword.isBlank() -> {
                            errorMessage = "Please enter your current password"
                        }
                        newPassword.isBlank() -> {
                            errorMessage = "Please enter a new password"
                        }
                        newPassword.length < 6 -> {
                            errorMessage = "New password must be at least 6 characters"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Passwords don't match"
                        }
                        else -> {
                            // All validations passed, proceed with API call
                            isLoading = true
                            errorMessage = null
                            
                            scope.launch {
                                try {
                                    // Create request body
                                    val request = ChangePasswordRequest(
                                        currentPassword = currentPassword,
                                        newPassword = newPassword
                                    )
                                    
                                    // Make API call
                                    val response = withContext(Dispatchers.IO) {
                                        ApiClient.userService.changePassword(request)
                                    }
                                    
                                    if (response.isSuccessful && response.body() != null) {
                                        // Success
                                        successMessage = response.body()?.message ?: "Password updated successfully"
                                        
                                        // Clear fields
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        
                                        // Auto navigate back after delay
                                        delay(1500)
                                        navController.popBackStack()
                                    } else {
                                        // API error
                                        errorMessage = response.errorBody()?.string() ?: "Failed to update password"
                                    }
                                } catch (e: Exception) {
                                    // Network or other error
                                    errorMessage = "Error: ${e.localizedMessage ?: "Unknown error"}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Update Password")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
} 