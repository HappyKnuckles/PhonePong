package org.phonepong.project.screens.game.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.phonepong.project.screens.game.core.GameColors
import org.phonepong.project.screens.game.core.TableSpecs

fun DrawScope.drawBall(
    project: Projector,
    ballX: Float,
    ballZ: Float,
    ballHeight: Float
) {
    val radius = TableSpecs.BALL_RADIUS

    val shadowPos = project(ballX, 0f, ballZ)
    val shadowScale = (1.0f - (ballHeight / 200f)).coerceIn(0.5f, 1.0f)
    val shadowAlpha = (0.5f - (ballHeight / 400f)).coerceIn(0.1f, 0.5f)

    drawCircle(
        color = Color.Black.copy(alpha = shadowAlpha),
        radius = radius * shadowScale,
        center = shadowPos
    )
    val visualPos = project(ballX, ballHeight, ballZ)
    val gradient = Brush.radialGradient(
        colors = listOf(Color.White, Color(0xFFDDDDDD), Color(0xFFAAAAAA)),
        center = Offset(visualPos.x - radius / 3, visualPos.y - radius / 3),
        radius = radius * 1.2f
    )
    drawCircle(gradient, radius, visualPos)
}