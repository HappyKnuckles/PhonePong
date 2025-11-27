package org.tabletennis.project.screens.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import org.tabletennis.project.screens.game.core.GameColors
import org.tabletennis.project.screens.game.core.TableSpecs
import org.tabletennis.project.screens.game.logic.BallPhysics
import org.tabletennis.project.screens.game.logic.GameCoordinates
import org.tabletennis.project.screens.game.ui.components.BigMessageOverlay
import org.tabletennis.project.screens.game.ui.components.ScoreOverlay
import org.tabletennis.project.network.WebSocketManager
import org.tabletennis.project.screens.game.render.*
import kotlin.random.Random

@Composable
fun GameScreen(
    webSocketManager: WebSocketManager,
    playerNumber: Int
) {
    // STATE
    var score1 by remember { mutableIntStateOf(0) }
    var score2 by remember { mutableIntStateOf(0) }
    var scoreMessage by remember { mutableStateOf("") }

    var ballX by remember { mutableFloatStateOf(0f) }
    var ballY by remember { mutableFloatStateOf(0f) }
    var isMovingToPositive by remember { mutableStateOf(true) }

    var currentBounceDist by remember {
        mutableFloatStateOf(GameCoordinates.TableDims.LENGTH / 2 * 0.35f)
    }

    // NETWORK EVENTS
    val coordinatesEvent by webSocketManager.coordinatesEvent.collectAsState()
    val scoreEvent by webSocketManager.scoreEvent.collectAsState()

    LaunchedEffect(coordinatesEvent) {
        coordinatesEvent?.let { event ->
            val (newX, newZ) = GameCoordinates.mapGameToTable(event.x, event.y)

            if (newZ != ballY) {
                isMovingToPositive = newZ > ballY
            }

            if ((ballY > 0 && newZ <= 0) || (ballY < 0 && newZ >= 0)) {
                val halfLength = GameCoordinates.TableDims.LENGTH / 2

                val minBounce = 30f
                val maxBounce = halfLength * 0.6f

                currentBounceDist = minBounce + Random.nextFloat() * (maxBounce - minBounce)
            }

            ballX = newX
            ballY = newZ
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

                        val relativeBallZ = if (playerNumber == 2) -ballY else ballY

                        val doDrawBall = {
                            if (ballX != 0f || ballY != 0f) {
                                val dynamicHeight = BallPhysics.calculateBallHeight(
                                    ballX = ballX,
                                    ballZ = ballY,
                                    isMovingToPositive = isMovingToPositive,
                                    bounceDist = currentBounceDist,
                                    halfTableLength = halfL,
                                    halfTableWidth = halfW,
                                    ballRadius = TableSpecs.BALL_RADIUS
                                )
                                drawBall(project, ballX, ballY, dynamicHeight)
                            }
                        }

                        if (relativeBallZ > netHeight) {
                            doDrawBall()
                            drawNet(project, halfW, netHeight)
                        } else {
                            drawNet(project, halfW, netHeight)
                            doDrawBall()
                        }
                    }
                }
        )

        ScoreOverlay(score1, score2, playerNumber)
        BigMessageOverlay(scoreMessage)
    }
}