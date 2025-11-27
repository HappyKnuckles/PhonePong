package org.tabletennis.project.screens.game.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.tabletennis.project.screens.game.core.GameColors
import org.tabletennis.project.screens.game.core.TableSpecs

fun DrawScope.drawBall(
    project: Projector,
    ballX: Float,
    ballY: Float,
    ballHeight: Float
) {
    val radius = TableSpecs.BALL_RADIUS

    val shadowPos = project(ballX + 5f, 0f, ballY + 5f)
    drawCircle(GameColors.BallShadow, radius * 0.8f, shadowPos)

    val visualPos = project(ballX, ballHeight, ballY)
    val gradient = Brush.radialGradient(
        colors = listOf(Color.White, Color(0xFFDDDDDD), Color(0xFFAAAAAA)),
        center = Offset(visualPos.x - radius / 3, visualPos.y - radius / 3),
        radius = radius * 1.2f
    )
    drawCircle(gradient, radius, visualPos)
}