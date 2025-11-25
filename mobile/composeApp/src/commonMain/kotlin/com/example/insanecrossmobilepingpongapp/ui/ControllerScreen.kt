package com.example.insanecrossmobilepingpongapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.insanecrossmobilepingpongapp.controller.ControllerViewModel
import com.example.insanecrossmobilepingpongapp.network.ConnectionState
import com.example.insanecrossmobilepingpongapp.util.formatFloat
import kotlin.math.abs

@Composable
fun ControllerScreen(viewModel: ControllerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showConnectionDialog by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Ping Pong Controller",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Connection controls
            ConnectionCard(
                connectionState = state.connectionState,
                serverUrl = state.serverUrl,
                onConnectClick = { showConnectionDialog = true },
                onDisconnectClick = { viewModel.disconnect() }
            )

            // Connection status
            StatusCard(
                isConnected = state.connectionState == ConnectionState.CONNECTED,
                isCalibrated = state.isCalibrated,
                isActive = state.isActive
            )

            // Main tilt indicator (2D circle with dot)
            TiltIndicator(
                tiltX = state.currentControl.tiltX,
                tiltY = state.currentControl.tiltY,
                intensity = state.currentControl.intensity,
                modifier = Modifier
                    .size(280.dp)
                    .weight(1f)
            )

            // Numeric values - Tilt
            ControlValuesCard(
                tiltX = state.currentControl.tiltX,
                tiltY = state.currentControl.tiltY,
                intensity = state.currentControl.intensity
            )

            // Numeric values - Swing/Acceleration
            SwingValuesCard(
                swingSpeed = state.currentControl.swingSpeed,
                swingDirectionX = state.currentControl.swingDirectionX,
                swingDirectionY = state.currentControl.swingDirectionY
            )

            // Calibration button
            Button(
                onClick = { viewModel.calibrate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (state.isCalibrated) "Recalibrate" else "Calibrate",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Connection dialog
        if (showConnectionDialog) {
            ConnectionDialog(
                currentUrl = state.serverUrl,
                currentToken = state.token,
                onDismiss = { showConnectionDialog = false },
                onConnect = { url, token ->
                    viewModel.connect(url, token)
                    showConnectionDialog = false
                }
            )
        }
    }
}

@Composable
private fun StatusCard(
    isConnected: Boolean,
    isCalibrated: Boolean,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusBadge("Sensor", if (isActive) Color.Green else Color.Gray)
            StatusBadge("Connected", if (isConnected) Color.Green else Color.Red)
            StatusBadge("Calibrated", if (isCalibrated) Color.Green else Color.Gray)
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TiltIndicator(
    tiltX: Float,
    tiltY: Float,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2

                // Draw outer circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.3f),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw crosshairs
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(centerX - radius, centerY),
                    end = Offset(centerX + radius, centerY),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(centerX, centerY - radius),
                    end = Offset(centerX, centerY + radius),
                    strokeWidth = 1.dp.toPx()
                )

                // Calculate dot position based on tilt
                val dotX = centerX + (tiltX * radius * 0.85f)
                val dotY = centerY + (tiltY * radius * 0.85f)

                // Draw intensity ring around dot
                if (intensity > 0.1f) {
                    drawCircle(
                        color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                        radius = intensity * 30.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                // Draw tilt dot
                val dotColor = when {
                    intensity > 0.7f -> Color(0xFF4CAF50) // Green for high intensity
                    intensity > 0.3f -> Color(0xFFFFEB3B) // Yellow for medium
                    else -> Color(0xFF2196F3) // Blue for low
                }

                drawCircle(
                    color = dotColor,
                    radius = 16.dp.toPx(),
                    center = Offset(dotX, dotY)
                )

                // Draw center reference dot
                drawCircle(
                    color = Color.Gray,
                    radius = 4.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}

@Composable
private fun ControlValuesCard(
    tiltX: Float,
    tiltY: Float,
    intensity: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ValueRow(label = "Tilt X (L/R)", value = tiltX)
            ValueRow(label = "Tilt Y (F/B)", value = tiltY)
            ValueRow(label = "Intensity", value = intensity)
        }
    }
}

@Composable
private fun SwingValuesCard(
    swingSpeed: Float,
    swingDirectionX: Float,
    swingDirectionY: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ValueRow(label = "Swing Speed", value = swingSpeed)
            ValueRow(label = "Direction X", value = swingDirectionX)
            ValueRow(label = "Direction Y", value = swingDirectionY)
        }
    }
}

@Composable
private fun ConnectionCard(
    connectionState: ConnectionState,
    serverUrl: String,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "URL: $serverUrl",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = if (connectionState == ConnectionState.CONNECTED) onDisconnectClick else onConnectClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (connectionState == ConnectionState.CONNECTED)
                        Color(0xFFF44336) else Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = when (connectionState) {
                        ConnectionState.DISCONNECTED -> "Connect"
                        ConnectionState.CONNECTING -> "Connecting..."
                        ConnectionState.CONNECTED -> "Disconnect"
                        ConnectionState.ERROR -> "Reconnect"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ConnectionDialog(
    currentUrl: String,
    currentToken: String,
    onDismiss: () -> Unit,
    onConnect: (String, String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    var token by remember { mutableStateOf(currentToken) }
    var selectedPlayer by remember { mutableStateOf(if (currentToken == "player2") 1 else 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Connect to Server")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Server URL") },
                    placeholder = { Text("ws://131.159.222.93:3000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Player Token:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Player 1 button
                    Button(
                        onClick = {
                            selectedPlayer = 0
                            token = "player1"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPlayer == 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Player 1",
                            color = if (selectedPlayer == 0)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Player 2 button
                    Button(
                        onClick = {
                            selectedPlayer = 1
                            token = "player2"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPlayer == 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Player 2",
                            color = if (selectedPlayer == 1)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Selected: $token",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConnect(url, token) },
                enabled = url.isNotBlank() && token.isNotBlank()
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ValueRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = if (value >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                trackColor = Color.Gray.copy(alpha = 0.2f),
            )
            // Numeric value
            Text(
                text = formatFloat(value, 2),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}
