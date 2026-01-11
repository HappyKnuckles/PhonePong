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
import com.example.insanecrossmobilepingpongapp.sensor.needsMotionPermissionRequest
import org.jetbrains.compose.resources.stringResource
import insanecrossmobilepingpongapp.composeapp.generated.resources.*

var requestMotionPermissionHandler: ((Boolean) -> Unit) -> Unit = { callback ->
    callback(true)
}

/**
 * Start menu screen where users enter lobby code and select their player role.
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
    
    // Initial check
    var permissionGranted by remember { mutableStateOf(!needsMotionPermissionRequest()) }
    var permissionDenied by remember { mutableStateOf(false) }
    
    val backgroundColor = if (isDarkTheme) {
        Brush.verticalGradient(colors = listOf(Color(0xFF1A1A2E), Color(0xFF0F0F1E)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFF0F4F8), Color(0xFFD9E2EC)))
    }

    val cardColor = if (isDarkTheme) Color(0xFF2A2A3E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF102A43)
    val subTextColor = if (isDarkTheme) Color(0xFFBBBBBB) else Color(0xFF486581)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Theme Toggle
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()) {
            IconButton(onClick = { onThemeToggle(!isDarkTheme) }) {
                Text(
                    text = if (isDarkTheme) "Light" else "Dark",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
        }

        Card(
            modifier = Modifier
                .width(600.dp)
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp)
            ) {
                // Title Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Mobile Controller",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF06D6A0),
                            letterSpacing = 2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Divider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                // Instructions
                Text(
                    text = "Enter the lobby code from your screen",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = subTextColor
                    ),
                    textAlign = TextAlign.Center
                )

                // Lobby Code Input
                OutlinedTextField(
                    value = lobbyCode,
                    onValueChange = { 
                        lobbyCode = it.uppercase().filter { char -> char.isLetter() }.take(4)
                    },
                    label = { Text("Lobby Code") },
                    placeholder = { Text("ABCD", fontSize = 28.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = Color(0xFF06D6A0),
                        unfocusedBorderColor = subTextColor.copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFF06D6A0),
                        unfocusedLabelColor = subTextColor,
                        cursorColor = Color(0xFF06D6A0)
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Player Selection Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SELECT YOUR PLAYER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subTextColor,
                            letterSpacing = 1.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PlayerSelectButton(
                            text = "Player 1",
                            color = Color(0xFFE63946),
                            isSelected = selectedRole == PlayerRole.PLAYER1,
                            onClick = { selectedRole = PlayerRole.PLAYER1 },
                            modifier = Modifier.weight(1f)
                        )

                        PlayerSelectButton(
                            text = "Player 2",
                            color = Color(0xFF06D6A0),
                            isSelected = selectedRole == PlayerRole.PLAYER2,
                            onClick = { selectedRole = PlayerRole.PLAYER2 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text(
                        text = "Match your screen player number",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDarkTheme) Color(0xFFFFAA00) else Color(0xFFB86E00),
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Motion Permission Button (Only visible if needed)
                if (!permissionGranted && needsMotionPermissionRequest()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                requestMotionPermissionHandler { success ->
                                    permissionGranted = success
                                    permissionDenied = !success
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Enable Motion Sensors",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Text(
                            text = "Required for paddle control",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDarkTheme) Color(0xFFAAAAFF) else Color(0xFF2196F3),
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                if (permissionDenied) {
                    Text(
                        text = "Permission denied. Check Safari Settings > Motion Access",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFF5555),
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Join Button
                Button(
                    onClick = { 
                        selectedRole?.let { role ->
                            if (!permissionGranted && needsMotionPermissionRequest()) {
                                requestMotionPermissionHandler { success ->
                                    permissionGranted = success
                                    permissionDenied = !success
                                    if (success) {
                                        onJoinLobby(lobbyCode, role)
                                    }
                                }
                            } else {
                                onJoinLobby(lobbyCode, role)
                            }
                        }
                    },
                    enabled = lobbyCode.length == 4 && selectedRole != null,
                    modifier = Modifier.fillMaxWidth().height(64.dp).navigationBarsPadding(),
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
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (lobbyCode.length == 4 && selectedRole != null) 8.dp else 2.dp
                    )
                ) {
                    Text(
                        text = "Join Game",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color.White) else null
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