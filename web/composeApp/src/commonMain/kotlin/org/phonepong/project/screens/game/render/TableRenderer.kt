package org.phonepong.project.screens.game.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import org.phonepong.project.screens.game.core.GameColors
import org.phonepong.project.screens.game.core.TableSpecs

typealias Projector = (x: Float, y: Float, z: Float) -> Offset

/**
 * Draws the static parts of the table (Legs, Shadow, Surface).
 * DOES NOT DRAW THE NET.
 */
fun DrawScope.drawTableBase(
    project: Projector,
    halfW: Float,
    halfL: Float
) {
    val floorY = -TableSpecs.TABLE_HEIGHT_FROM_GROUND
    val inset = TableSpecs.LEG_INSET
    val thickness = TableSpecs.TABLE_THICKNESS

    // 1. FLOOR SHADOW
    val s1 = project(-halfW, floorY, -halfL)
    val s2 = project(halfW, floorY, -halfL)
    val s3 = project(halfW, floorY, halfL)
    val s4 = project(-halfW, floorY, halfL)
    val shadowPath = Path().apply {
        moveTo(s1.x, s1.y); lineTo(s2.x, s2.y); lineTo(s3.x, s3.y); lineTo(s4.x, s4.y); close()
    }
    drawPath(shadowPath, GameColors.Shadow)

    // 2. LEGS
    val legsCoords = listOf(
        Triple(-halfW + inset, floorY, -halfL + inset),
        Triple(halfW - inset, floorY, -halfL + inset),
        Triple(-halfW + inset, floorY, halfL - inset),
        Triple(halfW - inset, floorY, halfL - inset)
    )
    legsCoords.forEach { (lx, ly, lz) ->
        val start = project(lx, 0f, lz)
        val end = project(lx, ly, lz)
        drawLine(GameColors.Leg, start, end, strokeWidth = TableSpecs.LEG_WIDTH)
        val hl = Offset(2f, 2f)
        drawLine(GameColors.LegHighlight, start + hl, end + hl, strokeWidth = TableSpecs.LEG_WIDTH / 3)
    }

    // 3. TABLE BODY
    val t1 = project(-halfW, 0f, -halfL)
    val t2 = project(halfW, 0f, -halfL)
    val t3 = project(halfW, 0f, halfL)
    val t4 = project(-halfW, 0f, halfL)

    val b1 = project(-halfW, -thickness, -halfL)
    val b2 = project(halfW, -thickness, -halfL)
    val b3 = project(halfW, -thickness, halfL)
    val b4 = project(-halfW, -thickness, halfL)

    val tableBottomPath = Path().apply {
        moveTo(b1.x, b1.y); lineTo(b2.x, b2.y); lineTo(b3.x, b3.y); lineTo(b4.x, b4.y); close()
    }
    drawPath(tableBottomPath, GameColors.TableBottom)

    val frontSide = Path().apply { moveTo(t1.x, t1.y); lineTo(t2.x, t2.y); lineTo(b2.x, b2.y); lineTo(b1.x, b1.y); close() }
    val backSide = Path().apply { moveTo(t3.x, t3.y); lineTo(t4.x, t4.y); lineTo(b4.x, b4.y); lineTo(b3.x, b3.y); close() }
    val leftSide = Path().apply { moveTo(t4.x, t4.y); lineTo(t1.x, t1.y); lineTo(b1.x, b1.y); lineTo(b4.x, b4.y); close() }
    val rightSide = Path().apply { moveTo(t2.x, t2.y); lineTo(t3.x, t3.y); lineTo(b3.x, b3.y); lineTo(b2.x, b2.y); close() }

    drawPath(frontSide, GameColors.TableSide)
    drawPath(backSide, GameColors.TableSide)
    drawPath(leftSide, GameColors.TableSide)
    drawPath(rightSide, GameColors.TableSide)

    val tablePath = Path().apply {
        moveTo(t1.x, t1.y); lineTo(t2.x, t2.y); lineTo(t3.x, t3.y); lineTo(t4.x, t4.y); close()
    }
    val tableGradient = Brush.linearGradient(
        colors = listOf(GameColors.TableDark, GameColors.TableBase),
        start = t1, end = t4
    )
    drawPath(tablePath, tableGradient)
    drawPath(tablePath, Color.White, style = Stroke(width = 2f))

    val cStart = project(0f, 0f, -halfL)
    val cEnd = project(0f, 0f, halfL)
    drawLine(GameColors.CenterLine, cStart, cEnd, strokeWidth = 2f)
}

fun DrawScope.drawNet(
    project: Projector,
    halfW: Float,
    netHeight: Float
) {
    val netZ = 0f
    val netThickness = 10f

    val zFront = netZ - (netThickness / 2)
    val zBack = netZ + (netThickness / 2)

    val netOverhang = TableSpecs.NET_OVERHANG
    val netLeftX = -halfW - netOverhang
    val netRightX = halfW + netOverhang

    // NET POSTS
    val pLeftBotFront = project(netLeftX, 0f, zFront)
    val pLeftTopFront = project(netLeftX, netHeight, zFront)

    // BOTTOM TAPE
    val tapeBotLeftFront = project(netLeftX, 0f, zFront)
    val tapeBotRightFront = project(netRightX, 0f, zFront)

    drawLine(
        GameColors.NetTape.copy(alpha = 0.9f),
        tapeBotLeftFront,
        tapeBotRightFront,
        strokeWidth = 2f
    )

    drawLine(GameColors.NetPost, pLeftBotFront, pLeftTopFront, strokeWidth = 8f, cap = StrokeCap.Square)

    val pRightBotFront = project(netRightX, 0f, zFront)
    val pRightTopFront = project(netRightX, netHeight, zFront)

    drawLine(GameColors.NetPost, pRightBotFront, pRightTopFront, strokeWidth = 8f, cap = StrokeCap.Square)

    // MESH
    val meshColor = GameColors.NetMesh

    // Vertical Mesh Lines
    for (i in 1 until 20) {
        val fraction = i.toFloat() / 20
        val xPos = netLeftX + (netRightX - netLeftX) * fraction
        drawLine(
            meshColor,
            project(xPos, netHeight, netZ),
            project(xPos, 0f, netZ),
            strokeWidth = 0.8f
        )
    }
    // Horizontal Mesh Lines
    for (i in 1 until 5) {
        val fraction = i.toFloat() / 5
        val yPos = netHeight * fraction
        drawLine(
            meshColor,
            project(netLeftX, yPos, netZ),
            project(netRightX, yPos, netZ),
            strokeWidth = 0.8f
        )
    }

    // TOP TAPE
    val tapeTopLeftFront  = project(netLeftX, netHeight, zFront)
    val tapeTopRightFront = project(netRightX, netHeight, zFront)
    val tapeTopRightBack  = project(netRightX, netHeight, zBack)
    val tapeTopLeftBack   = project(netLeftX, netHeight, zBack)

    val tapePath = Path().apply {
        moveTo(tapeTopLeftFront.x, tapeTopLeftFront.y)
        lineTo(tapeTopRightFront.x, tapeTopRightFront.y)
        lineTo(tapeTopRightBack.x, tapeTopRightBack.y)
        lineTo(tapeTopLeftBack.x, tapeTopLeftBack.y)
        close()
    }

    drawPath(tapePath, GameColors.NetTape)

    drawLine(
        GameColors.NetTape,
        tapeTopLeftFront,
        tapeTopRightFront,
        strokeWidth = 4f
    )

    drawLine(Color.Gray, tapeTopLeftFront, tapeTopLeftBack, strokeWidth = 1f)
    drawLine(Color.Gray, tapeTopRightFront, tapeTopRightBack, strokeWidth = 1f)
}