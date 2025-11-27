package org.tabletennis.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.tabletennis.project.screens.game.ui.screens.GameScreen
import org.tabletennis.project.network.WebSocketManager
import org.tabletennis.project.screens.GameFlow

private const val DEVELOPMENT_MODE = false

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF222222))
        ) {
            if (DEVELOPMENT_MODE) {
                val webSocketManager = remember { WebSocketManager() }

                GameScreen(
                    webSocketManager = webSocketManager,
                    playerNumber = 1
                )
            }
            else {
                GameFlow()
            }
        }
    }
}
