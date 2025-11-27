package org.tabletennis.project.screens.game.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import org.tabletennis.project.screens.game.core.GameColors
import org.tabletennis.project.screens.game.core.TableSpecs

typealias Projector = (x: Float, y: Float, z: Float) -> Offset

/**
 * Draws the complete Ping Pong table entity: Legs, Shadow, Body, and Net.
 */
fun DrawScope.drawTable(
    project: Projector,
    halfW: Float,
    halfL: Float,
    netHeight: Float
) {
    val floorY = -TableSpecs.TABLE_HEIGHT_FROM_GROUND
    val inset = TableSpecs.LEG_INSET
    val thickness = TableSpecs.TABLE_THICKNESS

    // LEGS
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

    // FLOOR SHADOW
    val s1 = project(-halfW, floorY, -halfL)
    val s2 = project(halfW, floorY, -halfL)
    val s3 = project(halfW, floorY, halfL)
    val s4 = project(-halfW, floorY, halfL)

    val shadowPath = Path().apply {
        moveTo(s1.x, s1.y); lineTo(s2.x, s2.y); lineTo(s3.x, s3.y); lineTo(s4.x, s4.y); close()
    }
    drawPath(shadowPath, GameColors.Shadow)

    // TABLE BODY
    // Top Corners (Y = 0)
    val t1 = project(-halfW, 0f, -halfL)
    val t2 = project(halfW, 0f, -halfL)
    val t3 = project(halfW, 0f, halfL)
    val t4 = project(-halfW, 0f, halfL)

    // Bottom Corners (Y = -thickness)
    val b1 = project(-halfW, -thickness, -halfL)
    val b2 = project(halfW, -thickness, -halfL)
    val b3 = project(halfW, -thickness, halfL)
    val b4 = project(-halfW, -thickness, halfL)

    // Draw Bottom Panel
    val tableBottomPath = Path().apply {
        moveTo(b1.x, b1.y); lineTo(b2.x, b2.y); lineTo(b3.x, b3.y); lineTo(b4.x, b4.y); close()
    }
    drawPath(tableBottomPath, GameColors.TableBottom)

    // Draw Sides
    val frontSide = Path().apply { moveTo(t1.x, t1.y); lineTo(t2.x, t2.y); lineTo(b2.x, b2.y); lineTo(b1.x, b1.y); close() }
    val backSide = Path().apply { moveTo(t3.x, t3.y); lineTo(t4.x, t4.y); lineTo(b4.x, b4.y); lineTo(b3.x, b3.y); close() }
    val leftSide = Path().apply { moveTo(t4.x, t4.y); lineTo(t1.x, t1.y); lineTo(b1.x, b1.y); lineTo(b4.x, b4.y); close() }
    val rightSide = Path().apply { moveTo(t2.x, t2.y); lineTo(t3.x, t3.y); lineTo(b3.x, b3.y); lineTo(b2.x, b2.y); close() }

    drawPath(frontSide, GameColors.TableSide)
    drawPath(backSide, GameColors.TableSide)
    drawPath(leftSide, GameColors.TableSide)
    drawPath(rightSide, GameColors.TableSide)

    // Draw Top Surface
    val tablePath = Path().apply {
        moveTo(t1.x, t1.y); lineTo(t2.x, t2.y); lineTo(t3.x, t3.y); lineTo(t4.x, t4.y); close()
    }
    val tableGradient = Brush.linearGradient(
        colors = listOf(GameColors.TableDark, GameColors.TableBase),
        start = t1, end = t4
    )
    drawPath(tablePath, tableGradient)
    drawPath(tablePath, Color.White, style = Stroke(width = 2f))

    // Draw Center Line
    val cStart = project(0f, 0f, -halfL)
    val cEnd = project(0f, 0f, halfL)
    drawLine(GameColors.CenterLine, cStart, cEnd, strokeWidth = 2f)

    // NET
    val netZ = 0f
    val netLeftX = -halfW - TableSpecs.NET_OVERHANG
    val netRightX = halfW + TableSpecs.NET_OVERHANG

    // Vertical Mesh Lines
    for (i in 1 until 20) {
        val fraction = i.toFloat() / 20
        val xPos = netLeftX + (netRightX - netLeftX) * fraction
        drawLine(
            GameColors.NetMesh,
            project(xPos, netHeight, netZ),
            project(xPos, 0f, netZ),
            strokeWidth = 1f
        )
    }

    // Horizontal Mesh Lines
    for (i in 1 until 5) {
        val fraction = i.toFloat() / 5
        val yPos = netHeight * fraction
        drawLine(
            GameColors.NetMesh,
            project(netLeftX, yPos, netZ),
            project(netRightX, yPos, netZ),
            strokeWidth = 1f
        )
    }

    // Posts & Tape
    val pLeftBot = project(netLeftX, 0f, netZ)
    val pRightBot = project(netRightX, 0f, netZ)
    val pLeftTop = project(netLeftX, netHeight, netZ)
    val pRightTop = project(netRightX, netHeight, netZ)

    drawLine(GameColors.NetPost, pLeftBot, pLeftTop, strokeWidth = 6f)
    drawLine(GameColors.NetPost, pRightBot, pRightTop, strokeWidth = 6f)
    drawLine(GameColors.NetTape, pLeftTop, pRightTop, strokeWidth = 3f)
    drawLine(GameColors.NetTape, pLeftBot, pRightBot, strokeWidth = 2f)
}