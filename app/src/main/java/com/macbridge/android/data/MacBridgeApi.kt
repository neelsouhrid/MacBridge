package com.macbridge.android.data

import com.macbridge.android.data.models.ApiResponse
import com.macbridge.android.data.models.AppInfo
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the MacBridge HTTP server running on macOS.
 * All endpoints use GET requests with query parameters.
 */
interface MacBridgeApi {

    /** Health check / connection test */
    @GET("/ping")
    suspend fun ping(): ApiResponse

    /** Get list of all running apps with their window titles */
    @GET("/apps")
    suspend fun getApps(): List<AppInfo>

    /** Switch to a specific app, optionally to a specific window */
    @GET("/switch")
    suspend fun switchApp(
        @Query("app") app: String,
        @Query("window") window: String? = null
    ): ApiResponse

    /** Set system volume (0-100) */
    @GET("/volume")
    suspend fun setVolume(
        @Query("level") level: Int
    ): ApiResponse

    /** Set screen brightness (0.0-1.0) */
    @GET("/brightness")
    suspend fun setBrightness(
        @Query("level") level: Double
    ): ApiResponse

    /** Toggle mute on/off */
    @GET("/mute")
    suspend fun toggleMute(): ApiResponse

    /** Put Mac to sleep */
    @GET("/sleep")
    suspend fun sleep(): ApiResponse
}
