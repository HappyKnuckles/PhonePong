package org.phonepong.project.screens.game.core

import androidx.compose.ui.graphics.Color

object GameColors {
    val FloorGradient = listOf(Color(0xFF333333), Color(0xFF111111))
    val TableBase = Color(0xFF1565C0)
    val TableDark = Color(0xFF0D47A1)
    val TableSide = Color(0xFF0A3880)
    val TableBottom = Color(0xFF072A5C)
    val CenterLine = Color.White.copy(alpha = 0.8f)
    val NetPost = Color(0xFF444444)
    val NetMesh = Color.White.copy(alpha = 0.3f)
    val NetTape = Color.White
    val Leg = Color(0xFF222222)
    val LegHighlight = Color(0xFF444444)
    val Shadow = Color.Black.copy(alpha = 0.5f)
    val BallShadow = Color.Black.copy(alpha = 0.3f)
    val TextWhite = Color.White
    val TextGray = Color.Gray
    val MessageGold = Color(0xFFFFD700)
}

object TableSpecs {
    const val TABLE_HEIGHT_FROM_GROUND = 760f
    const val TABLE_THICKNESS = 25f
    const val LEG_INSET = 50f
    const val LEG_WIDTH = 12f
    const val BALL_RADIUS = 10f
    const val NET_OVERHANG = 15f
}