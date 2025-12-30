package com.example.insanecrossmobilepingpongapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insanecrossmobilepingpongapp.model.PlayerRole
import org.jetbrains.compose.resources.stringResource
import insanecrossmobilepingpongapp.composeapp.generated.resources.*

/**
 * Start menu screen where users enter lobby code and select their player role.
 * Player role must match the web host screen to avoid mirrored view.
 */
@Composable
fun MenuScreen(
    onJoinLobby: (String, PlayerRole) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var lobbyCode by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<PlayerRole?>(null) }
    
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
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
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Enter lobby code from your screen",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 16.sp,
                        color = subTextColor
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Lobby Code Input
            OutlinedTextField(
                value = lobbyCode,
                onValueChange = { 
                    // Only allow uppercase letters, max 4 characters
                    lobbyCode = it.uppercase().filter { char -> char.isLetter() }.take(4)
                },
                label = { Text("Lobby Code") },
                placeholder = { Text("ABCD") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = Color(0xFF06D6A0),
                    unfocusedBorderColor = subTextColor,
                    focusedLabelColor = Color(0xFF06D6A0),
                    unfocusedLabelColor = subTextColor,
                    cursorColor = Color(0xFF06D6A0)
                ),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 8.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            // Player Selection Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select your player (match your screen)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = subTextColor
                    ),
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Player 1 Button (Red)
                    PlayerSelectButton(
                        text = "Player 1",
                        color = Color(0xFFE63946),
                        isSelected = selectedRole == PlayerRole.PLAYER1,
                        onClick = { selectedRole = PlayerRole.PLAYER1 },
                        modifier = Modifier.weight(1f)
                    )

                    // Player 2 Button (Green)
                    PlayerSelectButton(
                        text = "Player 2",
                        color = Color(0xFF06D6A0),
                        isSelected = selectedRole == PlayerRole.PLAYER2,
                        onClick = { selectedRole = PlayerRole.PLAYER2 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Join Button
            Button(
                onClick = { 
                    selectedRole?.let { role ->
                        onJoinLobby(lobbyCode, role)
                    }
                },
                enabled = lobbyCode.length == 4 && selectedRole != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedRole) {
                        PlayerRole.PLAYER1 -> Color(0xFFE63946)
                        PlayerRole.PLAYER2 -> Color(0xFF06D6A0)
                        null -> Color(0xFF888888)
                    },
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF888888).copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "Join Game",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Info Text
            Text(
                text = "⚠️ Make sure to select the same player\nas shown on your web screen",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isDarkTheme) Color(0xFFFFAA00) else Color(0xFFB86E00),
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Player selection button with selected state.
 */
@Composable
private fun PlayerSelectButton(
    text: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else color.copy(alpha = 0.3f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp,
            pressedElevation = 4.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(3.dp, Color.White)
        } else null
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}
