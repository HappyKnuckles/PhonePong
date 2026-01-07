package org.tabletennis.project.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaitingScreen(playerNumber: Int, lobbyId: String? = null, isSingleplayer: Boolean = false) {
    val playerColor = when (playerNumber) {
        1 -> Color(0xFFF44336)
        2 -> Color(0xFF4CAF50)
        else -> Color(0xFF06D6A0)
    }

    val playerText = when {
        isSingleplayer -> "Singleplayer Mode"
        playerNumber == 0 -> "Connecting..."
        else -> "You are Player $playerNumber"
    }

    val waitingText = if (isSingleplayer) {
        "Waiting for you to connect your phone..."
    } else {
        "Waiting for opponent..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF333333)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
            ) {
                // Show lobby code prominently
                if (!lobbyId.isNullOrBlank()) {
                    Text(
                        "LOBBY CODE",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                    Text(
                        lobbyId,
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                Text(
                    playerText,
                    fontSize = 28.sp,
                    color = playerColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    waitingText,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                CircularProgressIndicator(
                    color = playerColor,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                // Instructions for mobile user
                if (playerNumber > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = playerColor.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "📱 On your phone:",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Enter code: ${lobbyId ?: "----"}",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSingleplayer) {
                                Text(
                                    "Select: Player 1 (You vs Bot)",
                                    fontSize = 16.sp,
                                    color = playerColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "🤖 Bot difficulty: Medium",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    "Select: Player $playerNumber",
                                    fontSize = 16.sp,
                                    color = playerColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}