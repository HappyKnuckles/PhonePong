package org.phonepong.project.screens.game.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.phonepong.project.screens.game.core.GameColors

@Composable
fun ScoreOverlay(score1: Int, score2: Int, playerNumber: Int, lobbyCode: String = "") {
    Box(modifier = Modifier.fillMaxSize()) {
        // Lobby code badge - top left
        if (lobbyCode.isNotEmpty()) {
            LobbyCodeBadge(
                lobbyCode = lobbyCode,
                isDarkTheme = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }
        
        // Score - top center (enhanced design)
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xAA2A2A3E) // Semi-transparent
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "$score1 : $score2",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You are Player $playerNumber",
                    color = when (playerNumber) {
                        1 -> Color(0xFFF44336) // Red
                        2 -> Color(0xFF4CAF50) // Green
                        else -> GameColors.TextGray
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BigMessageOverlay(message: String) {
    if (message.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-50).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = GameColors.MessageGold,
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 72.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )
        }
    }
}