package com.example.safehouse.data.network

import com.example.safehouse.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("api/auth/request-verification")
    suspend fun requestVerification(@Body phone: PhoneRequest): Response<OtpResponse>
    
    @POST("api/auth/verify-phone")
    suspend fun verifyPhone(@Body verifyRequest: VerifyPhoneRequest): Response<VerifyResponse>
    
    @POST("api/auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
    
    @POST("api/auth/request-login-otp")
    suspend fun requestLoginOtp(@Body phone: PhoneRequest): Response<OtpResponse>
    
    @POST("api/auth/login-with-otp")
    suspend fun loginWithOtp(@Body otpLoginRequest: OtpLoginRequest): Response<AuthResponse>
}

interface UserApiService {
    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>
    
    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body profileRequest: UpdateProfileRequest): Response<UserProfileResponse>
    
    @PUT("api/user/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>
    
    @PUT("api/user/preferences")
    suspend fun updatePreferences(@Body preferencesRequest: PreferencesRequest): Response<PreferencesResponse>
    
    @Multipart
    @POST("api/user/profile-image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): Response<ImageUploadResponse>
    
    @GET("api/user/reservation-history")
    suspend fun getReservationHistory(): Response<ReservationHistoryResponse>
}

interface LockerApiService {
    @GET("api/lockers/locations")
    suspend fun getLockerLocations(): Response<LockerLocationsResponse>
    
    @GET("api/lockers/nearby")
    suspend fun getNearbyLockers(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 5
    ): Response<LockerLocationsResponse>
    
    @GET("api/lockers/location/{locationId}")
    suspend fun getLocationDetails(
        @Path("locationId") locationId: String
    ): Response<LocationDetailsResponse>
    
    @POST("api/lockers/reserve")
    suspend fun reserveLocker(@Body request: ReserveLockerRequest): Response<ReservationResponse>
    
    @GET("api/lockers/reservations")
    suspend fun getActiveReservations(): Response<ActiveReservationsResponse>
    
    @POST("api/lockers/extend")
    suspend fun extendReservation(@Body request: ExtendReservationRequest): Response<ReservationResponse>
    
    @POST("api/lockers/release/{reservationId}")
    suspend fun releaseLocker(@Path("reservationId") reservationId: String): Response<MessageResponse>
} 