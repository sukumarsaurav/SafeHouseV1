package com.example.safehouse.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safehouse.data.models.PhoneRequest
import com.example.safehouse.data.models.VerifyPhoneRequest
import com.example.safehouse.data.network.ApiClient
import com.example.safehouse.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OtpScreen(phoneNumber: String, navController: NavController) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendEnabled by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(60) }
    val coroutineScope = rememberCoroutineScope()
    
    // Only request OTP if we're not coming from signup
    // The OTP should have been sent during signup already
    LaunchedEffect(Unit) {
        // Just start the countdown timer for resend
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        resendEnabled = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verification Code",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "We've sent a 6-digit code to\n${formatPhoneNumberForDisplay(phoneNumber)}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // OTP Input Field
        BasicTextField(
            value = otp,
            onValueChange = { 
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    otp = it
                    errorMessage = null
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(6) { index ->
                        val char = when {
                            index >= otp.length -> ""
                            else -> otp[index].toString()
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = if (index < otp.length) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = {
                coroutineScope.launch {
                    if (otp.length < 6) {
                        errorMessage = "Please enter the complete 6-digit code"
                        return@launch
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    try {
                        val verifyRequest = VerifyPhoneRequest(phone = phoneNumber, otp = otp)
                        val response = ApiClient.authService.verifyPhone(verifyRequest)
                        
                        if (response.isSuccessful) {
                            // Navigate to home screen after successful verification
                            navController.navigate(Screen.Home.route) {
                                // Clear the back stack so user can't go back to auth screens
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            errorMessage = "Verification failed: ${response.errorBody()?.string() ?: "Invalid code"}"
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
                .height(56.dp),
            enabled = !isLoading && otp.length == 6
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Verify")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (resendEnabled) "Didn't receive the code? " else "Resend code in $secondsLeft seconds",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (resendEnabled) {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                errorMessage = null
                                
                                val phoneRequest = PhoneRequest(phone = phoneNumber)
                                val response = ApiClient.authService.requestVerification(phoneRequest)
                                
                                if (response.isSuccessful) {
                                    // Reset timer
                                    secondsLeft = 60
                                    resendEnabled = false
                                } else {
                                    errorMessage = "Failed to resend verification code. Please try again."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network error: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && resendEnabled
                ) {
                    Text("Resend Code")
                }
            } else if (secondsLeft > 0) {
                Text(
                    text = "Resend code in ${secondsLeft}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatPhoneNumberForDisplay(phoneNumber: String): String {
    // If the phone number starts with +91, format it nicely
    return if (phoneNumber.startsWith("+91")) {
        val nationalNumber = phoneNumber.substring(3) // Remove +91
        "+91 ${nationalNumber.take(5)} ${nationalNumber.takeLast(5)}"
    } else {
        phoneNumber
    }
} 