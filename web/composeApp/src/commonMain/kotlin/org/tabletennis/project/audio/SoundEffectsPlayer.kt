package org.tabletennis.project.audio

import androidx.compose.runtime.Composable
import org.tabletennis.project.network.WebSocketManager

/**
 * Platform-specific sound effects component
 */
@Composable
expect fun SoundEffectsPlayer(webSocketManager: WebSocketManager)
