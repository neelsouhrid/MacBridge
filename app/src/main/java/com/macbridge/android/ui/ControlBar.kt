package com.macbridge.android.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Custom slider colors for the dark theme
private val sliderActiveColor = Color(0xFF7B8CDE)
private val sliderInactiveColor = Color(0xFF1a1a2e)
private val sliderThumbColor = Color(0xFF9AADFF)

/**
 * Persistent bottom control bar with volume, brightness, mute, and sleep controls.
 * Features smooth gradient background and debounced slider interactions.
 */
@Composable
fun ControlBar(
    volumeLevel: Float,
    brightnessLevel: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    onSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSleepDialog by remember { mutableStateOf(false) }

    // Sleep confirmation dialog
    if (showSleepDialog) {
        AlertDialog(
            onDismissRequest = { showSleepDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Bedtime,
                    contentDescription = null,
                    tint = Color(0xFF7B8CDE),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Put Mac to Sleep?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Your Mac will go to sleep. You'll need to wake it manually to reconnect.",
                    color = Color(0xFF8899AA)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSleepDialog = false
                        onSleep()
                    }
                ) {
                    Text("Sleep", color = Color(0xFF7B8CDE), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepDialog = false }) {
                    Text("Cancel", color = Color(0xFF8899AA))
                }
            },
            containerColor = Color(0xFF1a1a2e),
            shape = RoundedCornerShape(24.dp)
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0a0a1a),
                            Color(0xFF0d0d20),
                            Color(0xFF111133)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Volume Slider ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        contentDescription = "Volume",
                        tint = if (isMuted) Color(0xFF666688) else Color(0xFF7B8CDE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Slider(
                        value = volumeLevel,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = sliderThumbColor,
                            activeTrackColor = sliderActiveColor,
                            inactiveTrackColor = sliderInactiveColor
                        ),
                        enabled = !isMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${volumeLevel.toInt()}",
                        color = Color(0xFF8899AA),
                        fontSize = 12.sp,
                        modifier = Modifier.width(28.dp)
                    )
                }

                // ── Brightness Slider ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = "Brightness",
                        tint = Color(0xFFE8B931),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Slider(
                        value = brightnessLevel,
                        onValueChange = onBrightnessChange,
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE8B931),
                            activeTrackColor = Color(0xFFB8911F),
                            inactiveTrackColor = sliderInactiveColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${brightnessLevel.toInt()}",
                        color = Color(0xFF8899AA),
                        fontSize = 12.sp,
                        modifier = Modifier.width(28.dp)
                    )
                }

                // ── Action Buttons ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute button
                    val muteColor by animateColorAsState(
                        targetValue = if (isMuted) Color(0xFFFF6B6B) else Color(0xFF7B8CDE),
                        animationSpec = tween(300),
                        label = "muteColor"
                    )

                    FilledIconButton(
                        onClick = onMuteToggle,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = muteColor.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeMute,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = muteColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Sleep button
                    FilledIconButton(
                        onClick = { showSleepDialog = true },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFFE8B931).copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bedtime,
                            contentDescription = "Sleep",
                            tint = Color(0xFFE8B931),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
