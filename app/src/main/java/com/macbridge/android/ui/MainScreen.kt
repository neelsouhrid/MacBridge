package com.macbridge.android.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onRefresh: () -> Unit,
    onAppTap: (String) -> Unit,
    onWindowTap: (String, String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    onSleep: () -> Unit,
    onDisconnect: () -> Unit,
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
                                onWindowTap = { window -> onWindowTap(app.name, window) },
                                onToggleExpand = { onToggleExpand(app.name) },
                                index = index
                            )
                        }
                    }
                }

                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
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
