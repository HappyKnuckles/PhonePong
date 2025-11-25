package com.example.insanecrossmobilepingpongapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insanecrossmobilepingpongapp.model.PlayerRole
import org.jetbrains.compose.resources.stringResource
import insanecrossmobilepingpongapp.composeapp.generated.resources.*

/**
 * Start menu screen where users select their player role.
 */
@Composable
fun MenuScreen(
    onPlayerSelected: (PlayerRole) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A2E),
                Color(0xFF0F0F1E)
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

    val textColor = if (isDarkTheme) Color.White else Color(0xFF102A43)
    val subTextColor = if (isDarkTheme) Color(0xFFBBBBBB) else Color(0xFF486581)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Theme Toggle
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(onClick = { onThemeToggle(!isDarkTheme) }) {
                Text(
                    text = if (isDarkTheme) "☀️" else "🌙",
                    fontSize = 24.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = stringResource(Res.string.choose_player),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        color = subTextColor
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Player Selection Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Player 1 Button (Red)
                PlayerButton(
                    text = stringResource(Res.string.player_1),
                    color = Color(0xFFE63946),
                    onClick = { onPlayerSelected(PlayerRole.PLAYER1) }
                )

                // Player 2 Button (Green)
                PlayerButton(
                    text = stringResource(Res.string.player_2),
                    color = Color(0xFF06D6A0),
                    onClick = { onPlayerSelected(PlayerRole.PLAYER2) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info Text
            Text(
                text = stringResource(Res.string.game_start_note),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isDarkTheme) Color(0xFF888888) else Color(0xFF627D98),
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Reusable player selection button with custom color.
 */
@Composable
private fun PlayerButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
