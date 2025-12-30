package org.tabletennis.project.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerSelectionScreen(
    onPlayerSelected: (Int) -> Unit
) {
    var selectedPlayerLabel by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222222)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Ping Pong Online",
            fontSize = 36.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Choose your Side",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- Player 1 Button ---
        Button(
            onClick = {
                selectedPlayerLabel = "Player 1"
                onPlayerSelected(1)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336)
            ),
            modifier = Modifier
                .width(220.dp)
                .height(60.dp)
        ) {
            Text("Player 1 (Red)", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Player 2 Button ---
        Button(
            onClick = {
                selectedPlayerLabel = "Player 2"
                onPlayerSelected(2)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier
                .width(220.dp)
                .height(60.dp)
        ) {
            Text("Player 2 (Green)", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Info: Ensure your opponent joins the same Lobby ID.",
            fontSize = 14.sp,
            color = Color.Yellow.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}