package com.macbridge.android.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.macbridge.android.data.models.AppInfo
import com.macbridge.android.utils.AppIconMapper

// Curated color palette for app card gradients
private val cardGradients = listOf(
    listOf(Color(0xFF1a1a2e), Color(0xFF16213e)),
    listOf(Color(0xFF0f3460), Color(0xFF1a1a2e)),
    listOf(Color(0xFF1b1b3a), Color(0xFF2a1b3d)),
    listOf(Color(0xFF162447), Color(0xFF1f4068)),
    listOf(Color(0xFF1a1a2e), Color(0xFF0f3460)),
    listOf(Color(0xFF2d132c), Color(0xFF1a1a2e)),
    listOf(Color(0xFF1b262c), Color(0xFF0f4c75)),
    listOf(Color(0xFF1a1a2e), Color(0xFF2b2d42)),
)

/**
 * A card representing a running macOS application.
 * Shows the app icon, name, and expand button for multi-window apps.
 * Features a scale animation on press for tactile feedback.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    appInfo: AppInfo,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onWindowTap: (String) -> Unit,
    onToggleExpand: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val gradient = cardGradients[index % cardGradients.size]
    val hasMultipleWindows = appInfo.windows.size > 1

    Column(
        modifier = modifier
            .scale(scale)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onLongClick = { onLongPress() },
                    onClick = {
                        if (hasMultipleWindows) {
                            onToggleExpand()
                        } else {
                            onTap()
                        }
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(gradient),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // App icon
                    Icon(
                        imageVector = AppIconMapper.getIcon(appInfo.name),
                        contentDescription = appInfo.name,
                        tint = Color(0xFF7B8CDE),
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // App name
                    Text(
                        text = appInfo.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Window count or single window title
                    if (appInfo.windows.size == 1) {
                        Text(
                            text = appInfo.windows.first(),
                            color = Color(0xFF8899AA),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (hasMultipleWindows) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${appInfo.windows.size} windows",
                                color = Color(0xFF7B8CDE),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Expand windows",
                                tint = Color(0xFF7B8CDE),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Expanded window list
        if (isExpanded && hasMultipleWindows) {
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111122)
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    appInfo.windows.forEach { windowTitle ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onWindowTap(windowTitle) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFF7B8CDE),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = windowTitle,
                                color = Color(0xFFCCDDEE),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
