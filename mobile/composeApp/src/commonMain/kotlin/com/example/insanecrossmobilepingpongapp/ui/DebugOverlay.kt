package com.example.insanecrossmobilepingpongapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insanecrossmobilepingpongapp.model.PaddleControl
import com.example.insanecrossmobilepingpongapp.network.ConnectionState
import com.example.insanecrossmobilepingpongapp.util.formatFloat
import kotlin.math.abs

/**
 * Debug overlay panel showing diagnostic information.
 * Slides in from the bottom and displays connection, tilt, and swing data.
 */
@Composable
fun DebugOverlay(
    connectionState: ConnectionState,
    serverUrl: String,
    isCalibrated: Boolean,
    isActive: Boolean,
    paddleControl: PaddleControl,
    onCalibrate: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Semi-transparent background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // Debug panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🐛 Debug Info",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Text(
                            text = "✕",
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Connection Status Card
                    DebugConnectionCard(
                        connectionState = connectionState,
                        serverUrl = serverUrl
                    )

                    // Status Card
                    DebugStatusCard(
                        isActive = isActive,
                        isCalibrated = isCalibrated,
                        isConnected = connectionState == ConnectionState.CONNECTED
                    )

                    // Tilt Data Card
                    DebugTiltCard(
                        tiltX = paddleControl.tiltX,
                        tiltY = paddleControl.tiltY,
                        intensity = paddleControl.intensity
                    )

                    // Swing Data Card
                    DebugSwingCard(
                        swingSpeed = paddleControl.swingSpeed,
                        swingDirectionX = paddleControl.swingDirectionX,
                        swingDirectionY = paddleControl.swingDirectionY
                    )

                    // Calibration Button
                    Button(
                        onClick = onCalibrate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text(
                            text = if (isCalibrated) "Recalibrate Sensor" else "Calibrate Sensor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Debug card showing connection information.
 */
@Composable
fun DebugConnectionCard(
    connectionState: ConnectionState,
    serverUrl: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🌐 Server Connection",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "URL: $serverUrl",
                fontSize = 12.sp,
                color = Color(0xFFBBBBBB)
            )
            Text(
                text = "Status: ${connectionState.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = when (connectionState) {
                    ConnectionState.CONNECTED -> Color(0xFF06D6A0)
                    ConnectionState.CONNECTING -> Color(0xFFFFA500)
                    ConnectionState.DISCONNECTED -> Color(0xFF888888)
                    ConnectionState.ERROR -> Color(0xFFE63946)
                }
            )
        }
    }
}

/**
 * Debug card showing sensor and calibration status.
 */
@Composable
fun DebugStatusCard(
    isActive: Boolean,
    isCalibrated: Boolean,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DebugStatusBadge("Sensor", if (isActive) Color(0xFF06D6A0) else Color.Gray)
            DebugStatusBadge("Connected", if (isConnected) Color(0xFF06D6A0) else Color(0xFFE63946))
            DebugStatusBadge("Calibrated", if (isCalibrated) Color(0xFF06D6A0) else Color.Gray)
        }
    }
}

@Composable
private fun DebugStatusBadge(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFBBBBBB)
        )
    }
}

/**
 * Debug card showing tilt control values.
 */
@Composable
fun DebugTiltCard(
    tiltX: Float,
    tiltY: Float,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🎮 Tilt Controls",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            DebugValueRow(label = "Tilt X (L/R)", value = tiltX)
            DebugValueRow(label = "Tilt Y (F/B)", value = tiltY)
            DebugValueRow(label = "Intensity", value = intensity)
        }
    }
}

/**
 * Debug card showing swing motion values.
 */
@Composable
fun DebugSwingCard(
    swingSpeed: Float,
    swingDirectionX: Float,
    swingDirectionY: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🏓 Swing Motion",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            DebugValueRow(label = "Swing Speed", value = swingSpeed)
            DebugValueRow(label = "Direction X", value = swingDirectionX)
            DebugValueRow(label = "Direction Y", value = swingDirectionY)
        }
    }
}

/**
 * Reusable debug value row with progress bar and numeric display.
 */
@Composable
private fun DebugValueRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFBBBBBB)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { abs(value) },
                modifier = Modifier
                    .width(100.dp)
                    .height(8.dp),
                color = if (value >= 0) Color(0xFF06D6A0) else Color(0xFFE63946),
                trackColor = Color.Gray.copy(alpha = 0.2f),
            )
            // Numeric value
            Text(
                text = formatFloat(value, 2),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}
