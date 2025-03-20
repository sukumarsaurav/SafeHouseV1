package com.example.safehouse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.safehouse.ui.screens.auth.LoginScreen
import com.example.safehouse.ui.screens.auth.OtpScreen
import com.example.safehouse.ui.screens.auth.SignupScreen
import com.example.safehouse.ui.screens.home.HomeScreen
import com.example.safehouse.ui.screens.locker.LockerDetailsScreen
import com.example.safehouse.ui.screens.locker.LockerLocationsScreen
import com.example.safehouse.ui.screens.locker.ReservationDetailsScreen
import com.example.safehouse.ui.screens.profile.ProfileScreen
import com.example.safehouse.ui.screens.profile.ReservationHistoryScreen
import com.example.safehouse.ui.screens.settings.ChangePasswordScreen
import com.example.safehouse.ui.screens.settings.PreferencesScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object OtpVerification : Screen("otp_verification/{phoneNumber}")
    object Home : Screen("home")
    object LockerLocations : Screen("locker_locations")
    object LockerDetails : Screen("locker_details/{locationId}")
    object ReservationDetails : Screen("reservation_details/{reservationId}")
    object Profile : Screen("profile")
    object ReservationHistory : Screen("reservation_history")
    object ChangePassword : Screen("change_password")
    object Preferences : Screen("preferences")
}

@Composable
fun SafeHouseNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }
        
        composable(
            Screen.OtpVerification.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(phoneNumber = phoneNumber, navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.LockerLocations.route) {
            LockerLocationsScreen(navController = navController)
        }
        
        composable(
            Screen.LockerDetails.route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
            LockerDetailsScreen(locationId = locationId, navController = navController)
        }
        
        composable(
            Screen.ReservationDetails.route,
            arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            ReservationDetailsScreen(reservationId = reservationId, navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.ReservationHistory.route) {
            ReservationHistoryScreen(navController = navController)
        }
        
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(navController = navController)
        }
        
        composable(Screen.Preferences.route) {
            PreferencesScreen(navController = navController)
        }
    }
} 