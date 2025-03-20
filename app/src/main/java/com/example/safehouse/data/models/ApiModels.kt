package com.example.safehouse.data.models

// Auth Request Models
data class PhoneRequest(val phone: String)

data class VerifyPhoneRequest(val phone: String, val otp: String) // Note: using "otp" not "code"

data class SignupRequest(
    val phone: String,
    val password: String,
    val fullName: String,
    val email: String
)

data class LoginRequest(val phone: String, val password: String)

data class OtpLoginRequest(val phone: String, val otp: String)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UpdateProfileRequest(
    val name: String,
    val email: String
)

data class PreferencesRequest(
    val notificationsEnabled: Boolean,
    val defaultDuration: Int? = null,
    val defaultLockerSize: String? = null
)

data class ReserveLockerRequest(
    val lockerId: String,
    val duration: Int,
    val startTime: String? = null
)

data class ExtendReservationRequest(
    val reservationId: String,
    val additionalHours: Int
)

// Auth Response Models
data class OtpResponse(
    val message: String,
    val expiresAt: String
)

data class VerifyResponse(
    val verified: Boolean,
    val message: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val profileImageUrl: String?
)

data class UserProfileResponse(
    val user: User,
    val preferences: UserPreferences
)

data class UserPreferences(
    val notificationsEnabled: Boolean,
    val defaultDuration: Int,
    val defaultLockerSize: String
)

data class PreferencesResponse(
    val preferences: UserPreferences
)

data class MessageResponse(
    val message: String
)

data class ImageUploadResponse(
    val imageUrl: String
)

data class Reservation(
    val id: String,
    val lockerNumber: String,
    val locationId: String,
    val locationName: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val cost: Double,
    val accessCode: String?
)

data class ReservationHistoryResponse(
    val reservations: List<Reservation>
)

data class ActiveReservationsResponse(
    val reservations: List<Reservation>
)

data class LockerLocation(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val totalLockers: Int,
    val availableLockers: Int,
    val distance: Double? = null
)

data class LockerLocationsResponse(
    val locations: List<LocationSummary>
)

data class LocationSummary(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val availableLockers: Int,
    val totalLockers: Int,
    val distanceKm: Double?
)

data class LocationDetailsResponse(
    val location: LocationDetail,
    val lockers: List<LockerDetail>
)

data class LocationDetail(
    val id: String,
    val name: String,
    val address: String,
    val description: String,
    val openingHours: String,
    val latitude: Double,
    val longitude: Double,
    val amenities: List<String>
)

data class LockerDetail(
    val id: String,
    val number: String,
    val size: String,
    val status: String,
    val hourlyRate: Double
)

data class Locker(
    val id: String,
    val number: String,
    val size: String,
    val status: String,
    val hourlyRate: Double
)

data class OpeningHours(
    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    val saturday: String,
    val sunday: String
)

data class ReservationResponse(
    val reservation: Reservation
) 