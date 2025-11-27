package org.tabletennis.project.screens.game.ui.components

import androidx.compose.foundation.layout.*
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
import org.tabletennis.project.screens.game.core.GameColors

@Composable
fun ScoreOverlay(score1: Int, score2: Int, playerNumber: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$score1 : $score2",
            color = GameColors.TextWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You are Player $playerNumber",
            color = GameColors.TextGray,
            fontSize = 16.sp
        )
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