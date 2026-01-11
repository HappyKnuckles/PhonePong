package org.phonepong.project.screens.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import org.phonepong.project.screens.game.render.Projector
import org.phonepong.project.screens.game.render.drawNet
import org.phonepong.project.screens.game.render.drawTableBase
import org.phonepong.project.screens.game.core.GameColors
import org.phonepong.project.screens.game.core.TableSpecs
import org.phonepong.project.screens.game.logic.GameCoordinates
import org.phonepong.project.screens.game.render.drawBall
import org.phonepong.project.screens.game.ui.components.BigMessageOverlay
import org.phonepong.project.screens.game.ui.components.ScoreOverlay
import org.phonepong.project.network.WebSocketManager
import org.tabletennis.project.screens.game.render.*
import kotlin.random.Random

@Composable
fun GameScreen(
    webSocketManager: WebSocketManager,
    playerNumber: Int,
    lobbyCode: String = ""
) {
    // STATE
    var score1 by remember { mutableIntStateOf(0) }
    var score2 by remember { mutableIntStateOf(0) }
    var scoreMessage by remember { mutableStateOf("") }

    var ballX by remember { mutableFloatStateOf(0f) }
    var ballDepth by remember { mutableFloatStateOf(0f) }
    var ballHeight by remember { mutableFloatStateOf(0f) }

    // NETWORK EVENTS
    val coordinatesEvent by webSocketManager.coordinatesEvent.collectAsState()
    val scoreEvent by webSocketManager.scoreEvent.collectAsState()

    LaunchedEffect(coordinatesEvent) {
        coordinatesEvent?.let { event ->
            val (newX, newDepth) = GameCoordinates.mapGameToTable(event.x, event.y)

            ballX = newX
            ballDepth = newDepth
            ballHeight = event.z
            scoreMessage = ""
        }
    }

    LaunchedEffect(scoreEvent) {
        scoreEvent?.let { event ->
            if (event.score.size >= 2) {
                val (p1Score, p2Score) = if (playerNumber == 1) {
                    event.score[0] to event.score[1]
                } else {
                    event.score[1] to event.score[0]
                }
                score1 = p1Score
                score2 = p2Score
                scoreMessage = event.message
            }
        }
    }

    // UI RENDER
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GameColors.FloorGradient)),
        contentAlignment = Alignment.TopCenter
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    
                    val halfW = GameCoordinates.TableDims.WIDTH / 2
                    val halfL = GameCoordinates.TableDims.LENGTH / 2
                    val netHeight = GameCoordinates.TableDims.NET_HEIGHT

                    val project: Projector = { x, y, z ->
                        val effectiveX = if (playerNumber == 2) -x else x
                        val effectiveZ = if (playerNumber == 2) -z else z
                        GameCoordinates.project3DToScreen(effectiveX, y, effectiveZ, w, h)
                    }

                    onDrawBehind {
                        drawTableBase(project, halfW, halfL)

                        val relativeBallDepth = if (playerNumber == 2) -ballDepth else ballDepth

                        val doDrawBall = {
                            if (ballX != 0f || ballDepth != 0f) {
                                drawBall(
                                    project = project,
                                    ballX = ballX,
                                    ballZ = ballDepth,
                                    ballHeight = ballHeight
                                )
                            }
                        }

                        if (relativeBallDepth  > netHeight) {
                            doDrawBall()
                            drawNet(project, halfW, netHeight)
                        } else {
                            drawNet(project, halfW, netHeight)
                            doDrawBall()
                        }
                    }
                }
        )

        ScoreOverlay(score1, score2, playerNumber, lobbyCode)
        BigMessageOverlay(scoreMessage)
    }
}