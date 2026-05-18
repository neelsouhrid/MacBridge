package com.macbridge.android.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.macbridge.android.viewmodel.ConnectionState

/**
 * Onboarding screen for entering the Mac's IP address and port.
 * Features animated gradient background, rotating logo, and connection status indicator.
 */
@Composable
fun OnboardingScreen(
    ipAddress: String,
    port: String,
    connectionState: ConnectionState,
    onIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // Rotating animation for the bridge icon
    val infiniteTransition = rememberInfiniteTransition(label = "logoRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsating glow for connecting state
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Subtle gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0f1a3a).copy(alpha = 0.6f),
                            Color.Black
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Logo Section ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF7B8CDE).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .rotate(rotation)
                )

                // Inner icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFF111133),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Hub,
                            contentDescription = "MacBridge",
                            tint = Color(0xFF7B8CDE),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Title ──
            Text(
                text = "MacBridge",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Control your Mac, remotely.",
                fontSize = 14.sp,
                color = Color(0xFF6677AA),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── IP Address Field ──
            OutlinedTextField(
                value = ipAddress,
                onValueChange = onIpChange,
                label = { Text("Mac IP Address", color = Color(0xFF6677AA)) },
                placeholder = { Text("192.168.1.100", color = Color(0xFF334466)) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Filled.Wifi,
                        contentDescription = null,
                        tint = Color(0xFF7B8CDE)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFAABBCC),
                    focusedBorderColor = Color(0xFF7B8CDE),
                    unfocusedBorderColor = Color(0xFF222244),
                    cursorColor = Color(0xFF7B8CDE),
                    focusedContainerColor = Color(0xFF0a0a1a),
                    unfocusedContainerColor = Color(0xFF0a0a1a)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Port Field ──
            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                label = { Text("Port", color = Color(0xFF6677AA)) },
                placeholder = { Text("5001", color = Color(0xFF334466)) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Filled.Tag,
                        contentDescription = null,
                        tint = Color(0xFF7B8CDE)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onConnect()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFAABBCC),
                    focusedBorderColor = Color(0xFF7B8CDE),
                    unfocusedBorderColor = Color(0xFF222244),
                    cursorColor = Color(0xFF7B8CDE),
                    focusedContainerColor = Color(0xFF0a0a1a),
                    unfocusedContainerColor = Color(0xFF0a0a1a)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Connect Button ──
            Button(
                onClick = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B8CDE)
                ),
                enabled = connectionState != ConnectionState.CONNECTING
            ) {
                if (connectionState == ConnectionState.CONNECTING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Connecting...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        Icons.Filled.Cable,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Connect to Mac",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Connection Status ──
            AnimatedVisibility(
                visible = connectionState != ConnectionState.DISCONNECTED,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (connectionState) {
                                ConnectionState.CONNECTED -> Color(0xFF1a3a1a)
                                ConnectionState.ERROR -> Color(0xFF3a1a1a)
                                ConnectionState.CONNECTING -> Color(0xFF1a1a3a)
                                else -> Color.Transparent
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (connectionState) {
                                    ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                                    ConnectionState.ERROR -> Color(0xFFFF5252)
                                    ConnectionState.CONNECTING -> Color(0xFF7B8CDE).copy(alpha = pulseAlpha)
                                    else -> Color.Gray
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (connectionState) {
                            ConnectionState.CONNECTED -> "Connected"
                            ConnectionState.ERROR -> "Connection Failed"
                            ConnectionState.CONNECTING -> "Connecting..."
                            ConnectionState.DISCONNECTED -> ""
                        },
                        color = when (connectionState) {
                            ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                            ConnectionState.ERROR -> Color(0xFFFF5252)
                            ConnectionState.CONNECTING -> Color(0xFF7B8CDE)
                            else -> Color.Gray
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Footer ──
            Text(
                text = "Make sure MacBridge is running\non your Mac and both devices\nare on the same Wi-Fi network.",
                color = Color(0xFF334466),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
