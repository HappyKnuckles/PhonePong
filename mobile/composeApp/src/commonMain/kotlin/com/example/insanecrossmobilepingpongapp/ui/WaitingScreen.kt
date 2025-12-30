package com.example.insanecrossmobilepingpongapp.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insanecrossmobilepingpongapp.model.PlayerRole
import org.jetbrains.compose.resources.stringResource
import insanecrossmobilepingpongapp.composeapp.generated.resources.*

@Composable
fun WaitingScreen(
    playerRole: PlayerRole?,
    lobbyId: String = "",
    isDarkTheme: Boolean
) {
    val playerNumber = when (playerRole) {
        PlayerRole.PLAYER1 -> 1
        PlayerRole.PLAYER2 -> 2
        null -> 0
    }
    
    val playerColor = when (playerNumber) {
        1 -> Color(0xFFF44336)
        2 -> Color(0xFF4CAF50)
        else -> Color(0xFF06D6A0)
    }

    val backgroundColor = if (isDarkTheme) Color(0xFF222222) else Color(0xFFF0F4F8)
    val cardColor = if (isDarkTheme) Color(0xFF333333) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Show lobby code if available
                if (lobbyId.isNotBlank()) {
                    Text(
                        "Lobby: $lobbyId",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (playerRole != null) {
                    Text(
                        stringResource(Res.string.you_are_player, playerNumber),
                        fontSize = 20.sp,
                        color = playerColor,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "Connecting...",
                        fontSize = 20.sp,
                        color = playerColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    stringResource(Res.string.waiting_for_second_player),
                    fontSize = 16.sp,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                CircularProgressIndicator(
                    color = playerColor,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
