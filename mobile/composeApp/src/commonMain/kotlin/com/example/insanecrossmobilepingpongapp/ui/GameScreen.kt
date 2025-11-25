package com.example.insanecrossmobilepingpongapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insanecrossmobilepingpongapp.model.PlayerRole
import com.example.insanecrossmobilepingpongapp.network.ConnectionState
import org.jetbrains.compose.resources.painterResource
import insanecrossmobilepingpongapp.composeapp.generated.resources.Res
import insanecrossmobilepingpongapp.composeapp.generated.resources.racket_black
import insanecrossmobilepingpongapp.composeapp.generated.resources.racket_red
import org.jetbrains.compose.resources.stringResource
import insanecrossmobilepingpongapp.composeapp.generated.resources.*

/**
 * Game screen showing the racket view and connection status.
 */
@Composable
fun GameScreen(
    playerRole: PlayerRole,
    connectionState: ConnectionState,
    isDebugVisible: Boolean,
    onToggleDebug: () -> Unit,
    onDisconnect: () -> Unit,
    debugContent: @Composable () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Determine racket resource based on player role
    val racketResource = when (playerRole) {
        PlayerRole.PLAYER1 -> Res.drawable.racket_red
        PlayerRole.PLAYER2 -> Res.drawable.racket_black
    }

    val backgroundBrush = if (isDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF16213E)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF0F4F8),
                Color(0xFFD9E2EC)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Image(
            painter = painterResource(racketResource),
            contentDescription = "Background Racket",
            modifier = Modifier
                .fillMaxSize()
                .scale(1.7f) //scale manually to fit size
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )

        // Top-Left Connection Badge
        ConnectionStatusBadge(
            connectionState = connectionState,
            playerRole = playerRole,
            isDarkTheme = isDarkTheme,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding() // Add padding for status bar
        )

        // Top-Right Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding(), // Add padding for status bar
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Debug Toggle Button
            FloatingActionButton(
                onClick = onToggleDebug,
                containerColor = if (isDebugVisible)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(56.dp)
            ) {
                Text(
                    text = if (isDebugVisible) "✕" else "🐞",
                    fontSize = 24.sp,
                    color = if (isDebugVisible) Color.White else Color.Gray
                )
            }
        }

        // Bottom Disconnect Button
        Button(
            onClick = onDisconnect,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp) // Increased bottom padding
                .navigationBarsPadding() // Add padding for navigation bar
                .widthIn(min = 200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE63946),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "✕",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.disconnect_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Debug Overlay
        if (isDebugVisible) {
            debugContent()
        }
    }
}


/**
 * Connection status badge showing player and connection state.
 */
@Composable
private fun ConnectionStatusBadge(
    connectionState: ConnectionState,
    playerRole: PlayerRole,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (connectionState) {
        ConnectionState.CONNECTED -> stringResource(Res.string.connected) to Color(0xFF06D6A0)
        ConnectionState.CONNECTING -> stringResource(Res.string.connecting) to Color(0xFFFFA500)
        ConnectionState.DISCONNECTED -> stringResource(Res.string.disconnected) to Color(0xFF888888)
        ConnectionState.ERROR -> stringResource(Res.string.error) to Color(0xFFE63946)
    }

    val containerColor = if (isDarkTheme) Color(0xFF2A2A3E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerRole.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = textColor
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
            }
        }
    }
}
