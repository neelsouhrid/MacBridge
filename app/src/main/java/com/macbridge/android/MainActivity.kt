package com.macbridge.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.macbridge.android.ui.MainScreen
import com.macbridge.android.ui.OnboardingScreen
import com.macbridge.android.ui.theme.MacBridgeTheme
import com.macbridge.android.viewmodel.ConnectionState
import com.macbridge.android.viewmodel.MainViewModel

/**
 * Single-activity entry point for the MacBridge Android app.
 * Manages navigation between Onboarding and Main screens based on connection state.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacBridgeTheme {
                MacBridgeApp()
            }
        }
    }
}

@Composable
fun MacBridgeApp(viewModel: MainViewModel = viewModel()) {
    val connectionState by viewModel.connectionState.collectAsState()
    val ipAddress by viewModel.ipAddress.collectAsState()
    val port by viewModel.port.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val volumeLevel by viewModel.volumeLevel.collectAsState()
    val brightnessLevel by viewModel.brightnessLevel.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val expandedApp by viewModel.expandedApp.collectAsState()
    val appToQuit by viewModel.appToQuit.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbar()
        }
    }

    val isConnected = connectionState == ConnectionState.CONNECTED

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF1a1a2e),
                        contentColor = Color(0xFFCCDDEE),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            AnimatedContent(
                targetState = isConnected,
                transitionSpec = {
                    if (targetState) {
                        // Connecting: slide in from right
                        (slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(400)
                        ) + fadeIn(tween(300)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(400)
                                ) + fadeOut(tween(200))
                            )
                    } else {
                        // Disconnecting: slide in from left
                        (slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(400)
                        ) + fadeIn(tween(300)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(400)
                                ) + fadeOut(tween(200))
                            )
                    }
                },
                label = "screenTransition"
            ) { connected ->
                if (connected) {
                    MainScreen(
                        apps = apps,
                        connectionState = connectionState,
                        isRefreshing = isRefreshing,
                        volumeLevel = volumeLevel,
                        brightnessLevel = brightnessLevel,
                        isMuted = isMuted,
                        expandedApp = expandedApp,
                        appToQuit = appToQuit,
                        isSearchOpen = isSearchOpen,
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        isSearching = isSearching,
                        onRefresh = viewModel::refreshApps,
                        onAppTap = { appName -> viewModel.switchApp(appName) },
                        onAppLongPress = viewModel::showQuitDialog,
                        onWindowTap = { appName, window -> viewModel.switchApp(appName, window) },
                        onToggleExpand = viewModel::toggleExpandedApp,
                        onVolumeChange = viewModel::setVolume,
                        onBrightnessChange = viewModel::setBrightness,
                        onMuteToggle = viewModel::toggleMute,
                        onSleep = viewModel::sleepMac,
                        onDisconnect = viewModel::disconnect,
                        onQuitConfirm = viewModel::quitApp,
                        onQuitDismiss = viewModel::dismissQuitDialog,
                        onSearchOpen = viewModel::openSearch,
                        onSearchClose = viewModel::closeSearch,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onLaunchApp = viewModel::launchApp
                    )
                } else {
                    OnboardingScreen(
                        ipAddress = ipAddress,
                        port = port,
                        connectionState = connectionState,
                        onIpChange = viewModel::updateIpAddress,
                        onPortChange = viewModel::updatePort,
                        onConnect = { viewModel.connect(ipAddress, port) }
                    )
                }
            }
        }
    }
}
