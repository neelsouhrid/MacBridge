package com.macbridge.android.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.macbridge.android.data.models.AppInfo
import com.macbridge.android.viewmodel.ConnectionState

/**
 * Main screen showing the app grid and persistent control bar.
 * Features pull-to-refresh, connection status in the top bar, and a 2-column grid of app cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    apps: List<AppInfo>,
    connectionState: ConnectionState,
    isRefreshing: Boolean,
    volumeLevel: Float,
    brightnessLevel: Float,
    isMuted: Boolean,
    expandedApp: String?,
    appToQuit: String?,
    isSearchOpen: Boolean,
    searchQuery: String,
    searchResults: List<String>,
    isSearching: Boolean,
    onRefresh: () -> Unit,
    onAppTap: (String) -> Unit,
    onAppLongPress: (String) -> Unit,
    onWindowTap: (String, String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    onSleep: () -> Unit,
    onDisconnect: () -> Unit,
    onQuitConfirm: (String) -> Unit,
    onQuitDismiss: () -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullRefreshState.startRefresh()
        } else {
            pullRefreshState.endRefresh()
        }
    }

    // ── Quit Confirmation Dialog ──
    if (appToQuit != null) {
        AlertDialog(
            onDismissRequest = onQuitDismiss,
            containerColor = Color(0xFF1a1a2e),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFAABBDD),
            title = {
                Text("Quit $appToQuit?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to force quit this app on your Mac?")
            },
            confirmButton = {
                TextButton(
                    onClick = { onQuitConfirm(appToQuit) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B6B))
                ) {
                    Text("Quit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onQuitDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6677AA))
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Search Overlay ──
    if (isSearchOpen) {
        SearchOverlay(
            query = searchQuery,
            results = searchResults,
            isSearching = isSearching,
            onQueryChange = onSearchQueryChange,
            onClose = onSearchClose,
            onLaunchApp = onLaunchApp
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Top App Bar ──
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0a0a1a),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp)
                        .padding(top = 48.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title + status
                        Column {
                            Text(
                                text = "MacBridge",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (connectionState == ConnectionState.CONNECTED)
                                                Color(0xFF4CAF50)
                                            else Color(0xFFFF5252)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${apps.size} apps running",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6677AA)
                                )
                            }
                        }

                        // Search + Disconnect buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Search button
                            IconButton(
                                onClick = onSearchOpen,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1a1a2e))
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search Apps",
                                    tint = Color(0xFF8899CC),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Disconnect button
                            IconButton(
                                onClick = onDisconnect,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1a1a2e))
                            ) {
                                Icon(
                                    Icons.Filled.LinkOff,
                                    contentDescription = "Disconnect",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── App Grid with Pull-to-Refresh ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .nestedScroll(pullRefreshState.nestedScrollConnection)
            ) {
                if (apps.isEmpty() && !isRefreshing) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.DesktopMac,
                                contentDescription = null,
                                tint = Color(0xFF334466),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No apps running",
                                color = Color(0xFF6677AA),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pull down to refresh",
                                color = Color(0xFF334466),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = apps,
                            key = { _, app -> app.name }
                        ) { index, app ->
                            AppCard(
                                appInfo = app,
                                isExpanded = expandedApp == app.name,
                                onTap = { onAppTap(app.name) },
                                onLongPress = { onAppLongPress(app.name) },
                                onWindowTap = { window -> onWindowTap(app.name, window) },
                                onToggleExpand = { onToggleExpand(app.name) },
                                index = index
                            )
                        }
                    }
                }

                // Only show PullToRefreshContainer when actively refreshing (fixes gray circle)
                if (pullRefreshState.isRefreshing || pullRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }

            // ── Control Bar ──
            ControlBar(
                volumeLevel = volumeLevel,
                brightnessLevel = brightnessLevel,
                isMuted = isMuted,
                onVolumeChange = onVolumeChange,
                onBrightnessChange = onBrightnessChange,
                onMuteToggle = onMuteToggle,
                onSleep = onSleep
            )
        }
    }
}

/**
 * Full-screen search overlay for finding and launching Mac apps.
 */
@Composable
fun SearchOverlay(
    query: String,
    results: List<String>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onLaunchApp: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(top = 48.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = {
                            Text(
                                "Search apps on Mac...",
                                color = Color(0xFF556688)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color(0xFF8899CC)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF6677AA)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF7B68EE),
                            focusedBorderColor = Color(0xFF7B68EE),
                            unfocusedBorderColor = Color(0xFF334466)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1a1a2e))
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results or hint
                when {
                    isSearching -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF7B68EE),
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                    query.length < 2 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF334466),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Type at least 2 characters to search",
                                    color = Color(0xFF556688),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    results.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.SearchOff,
                                    contentDescription = null,
                                    tint = Color(0xFF334466),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No apps found for \"$query\"",
                                    color = Color(0xFF556688),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(results) { appName ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onLaunchApp(appName) },
                                    color = Color(0xFF1a1a2e),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Apps,
                                            contentDescription = null,
                                            tint = Color(0xFF7B68EE),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = appName,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            Icons.Filled.OpenInNew,
                                            contentDescription = "Launch",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
