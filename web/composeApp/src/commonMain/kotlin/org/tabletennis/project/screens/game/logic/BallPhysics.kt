package org.tabletennis.project.screens.game.logic

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sin

object BallPhysics {

    fun calculateBallHeight(
        ballX: Float,
        ballZ: Float,
        isMovingToPositive: Boolean,
        bounceDist: Float,
        halfTableLength: Float,
        halfTableWidth: Float,
        ballRadius: Float
    ): Float {
        val distFromNet = ballZ.absoluteValue

        val minBounceDist = halfTableLength * 0.2f
        val effectiveBounceDist = if (bounceDist < minBounceDist) minBounceDist else bounceDist

        val isOnTargetSide = (isMovingToPositive && ballZ > 0) || (!isMovingToPositive && ballZ < 0)

        val paddleHitHeight = 50f
        val arcMaxHeight = 135f

        val totalFlightDistance = halfTableLength + effectiveBounceDist

        val currentDistTraveled = if (isOnTargetSide) {
            halfTableLength + distFromNet
        } else {
            halfTableLength - distFromNet
        }

        val progress = currentDistTraveled / totalFlightDistance

        if (progress < 1.0f || (isOnTargetSide && distFromNet >= effectiveBounceDist && !checkTableHit(ballX, effectiveBounceDist, distFromNet, halfTableWidth))) {

            val linearHeight = paddleHitHeight + (ballRadius - paddleHitHeight) * progress

            val shiftedProgress = progress.pow(0.85f)
            val arcHeight = arcMaxHeight * sin(shiftedProgress * PI).toFloat()

            return linearHeight + arcHeight
        }

        else {
            val virtualLandSpot = halfTableLength * 2.8f

            val totalBounceArcDist = virtualLandSpot - effectiveBounceDist

            val t = (distFromNet - effectiveBounceDist) / totalBounceArcDist

            val bouncePeakHeight = 180f

            return ballRadius + (bouncePeakHeight * 4f * t * (1f - t))
        }
    }

    private fun checkTableHit(ballX: Float, bounceDist: Float, currentDist: Float, halfWidth: Float): Boolean {
        if (currentDist == 0f) return true
        val impactX = ballX * (bounceDist / currentDist)
        return impactX.absoluteValue <= halfWidth
    }
}