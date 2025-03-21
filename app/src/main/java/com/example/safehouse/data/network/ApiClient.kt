package com.example.safehouse.data.network

import android.content.Context
import com.example.safehouse.data.local.DataStoreHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object that provides API service instances
 */
object ApiClient {
    private const val BASE_URL = "https://safehouse-n3k8.onrender.com/"
    private var dataStoreHelper: DataStoreHelper? = null
    
    // Initialize with context to set up DataStoreHelper
    fun initialize(context: Context) {
        dataStoreHelper = DataStoreHelper(context.applicationContext)
    }
    
    // Auth interceptor to add token to requests
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Skip auth for login and signup endpoints
        if (originalRequest.url.encodedPath.contains("/auth/login") ||
            originalRequest.url.encodedPath.contains("/auth/signup") ||
            originalRequest.url.encodedPath.contains("/auth/request-verification") ||
            originalRequest.url.encodedPath.contains("/auth/verify-phone") ||
            originalRequest.url.encodedPath.contains("/auth/request-login-otp") ||
            originalRequest.url.encodedPath.contains("/auth/login-with-otp")) {
            return@Interceptor chain.proceed(originalRequest)
        }
        
        // Get token synchronously (only for API calls)
        val token = runBlocking {
            dataStoreHelper?.authToken?.first()
        }
        
        // Add token to request if available
        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        chain.proceed(request)
    }
    
    // Create OkHttpClient with logging and timeout settings
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(authInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    // Create Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Create API service instances
    val authService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val userService: UserApiService = retrofit.create(UserApiService::class.java)
    val lockerService: LockerApiService = retrofit.create(LockerApiService::class.java)
} 