package com.macbridge.android.data

import com.macbridge.android.data.models.ApiResponse
import com.macbridge.android.data.models.AppInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Repository wrapping Retrofit calls to the MacBridge macOS server.
 * Handles API instance creation and exposes suspend functions for each endpoint.
 */
class MacRepository {

    private var api: MacBridgeApi? = null
    private var currentBaseUrl: String = ""

    /**
     * Creates / recreates the Retrofit instance when the server address changes.
     */
    fun configure(ip: String, port: Int) {
        val baseUrl = "http://$ip:$port/"

        if (baseUrl == currentBaseUrl && api != null) return

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(MacBridgeApi::class.java)
        currentBaseUrl = baseUrl
    }

    private fun requireApi(): MacBridgeApi {
        return api ?: throw IllegalStateException("MacBridge API not configured. Call configure() first.")
    }

    /** Test connectivity by calling /ping */
    suspend fun ping(): Result<ApiResponse> = runCatching {
        requireApi().ping()
    }

    /** Fetch running apps from /apps */
    suspend fun getApps(): Result<List<AppInfo>> = runCatching {
        requireApi().getApps()
    }

    /** Switch to an app (and optionally a specific window) */
    suspend fun switchApp(app: String, window: String? = null): Result<ApiResponse> = runCatching {
        requireApi().switchApp(app, window)
    }

    /** Set volume level (0-100) */
    suspend fun setVolume(level: Int): Result<ApiResponse> = runCatching {
        requireApi().setVolume(level)
    }

    /** Set brightness level (0.0-1.0) */
    suspend fun setBrightness(level: Double): Result<ApiResponse> = runCatching {
        requireApi().setBrightness(level)
    }

    /** Toggle mute */
    suspend fun toggleMute(): Result<ApiResponse> = runCatching {
        requireApi().toggleMute()
    }

    /** Put Mac to sleep */
    suspend fun sleep(): Result<ApiResponse> = runCatching {
        requireApi().sleep()
    }
}
