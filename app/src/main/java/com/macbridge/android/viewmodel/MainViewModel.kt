package com.macbridge.android.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.macbridge.android.data.MacRepository
import com.macbridge.android.data.models.AppInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Connection state for the MacBridge server.
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Main ViewModel for the MacBridge app.
 * Manages connection state, running apps, and control actions.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MacRepository()
    private val prefs = application.getSharedPreferences("macbridge_prefs", Context.MODE_PRIVATE)

    // Connection state
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Server address
    private val _ipAddress = MutableStateFlow(prefs.getString("ip_address", "") ?: "")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _port = MutableStateFlow(prefs.getString("port", "5001") ?: "5001")
    val port: StateFlow<String> = _port.asStateFlow()

    // Running apps
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    // Loading / Refreshing
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Mute state
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Snackbar messages
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Volume and Brightness
    private val _volumeLevel = MutableStateFlow(50f)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()

    private val _brightnessLevel = MutableStateFlow(50f)
    val brightnessLevel: StateFlow<Float> = _brightnessLevel.asStateFlow()

    // Debounce jobs
    private var volumeJob: Job? = null
    private var brightnessJob: Job? = null

    // Currently expanded app in the grid (for multi-window apps)
    private val _expandedApp = MutableStateFlow<String?>(null)
    val expandedApp: StateFlow<String?> = _expandedApp.asStateFlow()

    init {
        // Auto-connect if we have saved credentials
        val savedIp = _ipAddress.value
        val savedPort = _port.value
        if (savedIp.isNotBlank()) {
            connect(savedIp, savedPort)
        }
    }

    fun updateIpAddress(ip: String) {
        _ipAddress.value = ip
    }

    fun updatePort(port: String) {
        _port.value = port
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun toggleExpandedApp(appName: String) {
        _expandedApp.value = if (_expandedApp.value == appName) null else appName
    }

    /**
     * Connect to the MacBridge server at the given IP and port.
     */
    fun connect(ip: String, portStr: String) {
        val port = portStr.toIntOrNull() ?: 5001

        if (ip.isBlank()) {
            _snackbarMessage.value = "Please enter a valid IP address"
            return
        }

        // Save to SharedPreferences
        prefs.edit()
            .putString("ip_address", ip)
            .putString("port", portStr)
            .apply()

        _connectionState.value = ConnectionState.CONNECTING
        repository.configure(ip, port)

        viewModelScope.launch {
            val result = repository.getApps()
            result.fold(
                onSuccess = { appList ->
                    _apps.value = appList
                    _connectionState.value = ConnectionState.CONNECTED
                    _snackbarMessage.value = "Connected! Found ${appList.size} apps"
                },
                onFailure = { error ->
                    _connectionState.value = ConnectionState.ERROR
                    _snackbarMessage.value = "Connection failed: ${error.localizedMessage ?: "Unknown error"}"
                }
            )
        }
    }

    /**
     * Disconnect and go back to onboarding.
     */
    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        _apps.value = emptyList()
        _expandedApp.value = null
    }

    /**
     * Refresh the list of running apps (pull-to-refresh).
     */
    fun refreshApps() {
        _isRefreshing.value = true
        viewModelScope.launch {
            val result = repository.getApps()
            result.fold(
                onSuccess = { appList ->
                    _apps.value = appList
                    _connectionState.value = ConnectionState.CONNECTED
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Refresh failed: ${error.localizedMessage}"
                    _connectionState.value = ConnectionState.ERROR
                }
            )
            _isRefreshing.value = false
        }
    }

    /**
     * Switch to a specific app (and optionally a specific window).
     */
    fun switchApp(appName: String, windowTitle: String? = null) {
        viewModelScope.launch {
            val result = repository.switchApp(appName, windowTitle)
            result.fold(
                onSuccess = {
                    val target = if (windowTitle != null) "$appName → $windowTitle" else appName
                    _snackbarMessage.value = "Switched to $target"
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Switch failed: ${error.localizedMessage}"
                }
            )
        }
    }

    /**
     * Set volume with 300ms debounce.
     */
    fun setVolume(level: Float) {
        _volumeLevel.value = level
        volumeJob?.cancel()
        volumeJob = viewModelScope.launch {
            delay(300)
            val result = repository.setVolume(level.toInt())
            result.onFailure { error ->
                _snackbarMessage.value = "Volume error: ${error.localizedMessage}"
            }
        }
    }

    /**
     * Set brightness with 300ms debounce. Slider is 0-100, maps to 0.0-1.0.
     */
    fun setBrightness(level: Float) {
        _brightnessLevel.value = level
        brightnessJob?.cancel()
        brightnessJob = viewModelScope.launch {
            delay(300)
            val mappedLevel = (level / 100.0).coerceIn(0.0, 1.0)
            val result = repository.setBrightness(mappedLevel)
            result.onFailure { error ->
                _snackbarMessage.value = "Brightness error: ${error.localizedMessage}"
            }
        }
    }

    /**
     * Toggle mute/unmute.
     */
    fun toggleMute() {
        viewModelScope.launch {
            val result = repository.toggleMute()
            result.fold(
                onSuccess = { response ->
                    _isMuted.value = response.muted ?: !_isMuted.value
                    val state = if (_isMuted.value) "Muted" else "Unmuted"
                    _snackbarMessage.value = state
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Mute error: ${error.localizedMessage}"
                }
            )
        }
    }

    /**
     * Put Mac to sleep.
     */
    fun sleepMac() {
        viewModelScope.launch {
            val result = repository.sleep()
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Mac is going to sleep..."
                    _connectionState.value = ConnectionState.DISCONNECTED
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Sleep error: ${error.localizedMessage}"
                }
            )
        }
    }

    // ── App to quit (for long-press dialog) ──
    private val _appToQuit = MutableStateFlow<String?>(null)
    val appToQuit: StateFlow<String?> = _appToQuit.asStateFlow()

    fun showQuitDialog(appName: String) {
        _appToQuit.value = appName
    }

    fun dismissQuitDialog() {
        _appToQuit.value = null
    }

    /**
     * Quit an app by name.
     */
    fun quitApp(appName: String) {
        _appToQuit.value = null
        viewModelScope.launch {
            val result = repository.quitApp(appName)
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Quit $appName"
                    // Refresh app list after quitting
                    delay(500)
                    refreshApps()
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Quit failed: ${error.localizedMessage}"
                }
            )
        }
    }

    // ── Search state ──
    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun openSearch() {
        _isSearchOpen.value = true
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            val result = repository.searchApps(query)
            result.fold(
                onSuccess = { appNames ->
                    _searchResults.value = appNames
                },
                onFailure = {
                    _searchResults.value = emptyList()
                }
            )
            _isSearching.value = false
        }
    }

    /**
     * Launch an app by name.
     */
    fun launchApp(appName: String) {
        _isSearchOpen.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        viewModelScope.launch {
            val result = repository.launchApp(appName)
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Launched $appName"
                    delay(1000)
                    refreshApps()
                },
                onFailure = { error ->
                    _snackbarMessage.value = "Launch failed: ${error.localizedMessage}"
                }
            )
        }
    }
}

