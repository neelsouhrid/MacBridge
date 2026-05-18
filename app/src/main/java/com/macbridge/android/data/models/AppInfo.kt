package com.macbridge.android.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a running macOS application with its window titles.
 * Maps directly to the JSON returned by GET /apps:
 * [{"name": "Safari", "windows": ["YouTube", "Google"]}]
 */
data class AppInfo(
    @SerializedName("name")
    val name: String,

    @SerializedName("windows")
    val windows: List<String> = emptyList()
)

/**
 * Generic API response from MacBridge server.
 */
data class ApiResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("switched_to")
    val switchedTo: String? = null,

    @SerializedName("window")
    val window: String? = null,

    @SerializedName("volume")
    val volume: Int? = null,

    @SerializedName("brightness")
    val brightness: Double? = null,

    @SerializedName("muted")
    val muted: Boolean? = null,

    @SerializedName("action")
    val action: String? = null,

    @SerializedName("app")
    val app: String? = null
)
