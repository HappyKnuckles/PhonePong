package org.tabletennis.project.screens.game.logic

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.sin

object BallPhysics {

    /**
     * Calculates the height (Y-axis in 3D space) of the ball based on its position on the table.
     */
    fun calculateBallHeight(
        ballZ: Float,
        netHeight: Float,
        isMovingToPositive: Boolean,
        bounceDist: Float,
        halfTableLength: Float,
        ballRadius: Float
    ): Float {
        val distFromNet = ballZ.absoluteValue

        val isOnTargetSide = (isMovingToPositive && ballZ > 0) || (!isMovingToPositive && ballZ < 0)

        return if (isOnTargetSide) {
            if (distFromNet < bounceDist) {
                val t = distFromNet / bounceDist
                val startHeight = netHeight + 85f

                val linear = startHeight + (ballRadius - startHeight) * t
                val arcIntensity = 50f
                val arc = arcIntensity * sin(t * PI).toFloat()
                linear + arc
            } else {
                val virtualLandSpot = halfTableLength * 3.0f
                val totalArcDist = virtualLandSpot - bounceDist
                val t = (distFromNet - bounceDist) / totalArcDist
                val theoreticalPeak = 300f
                ballRadius + (theoreticalPeak * 4f * t * (1f - t))
            }
        } else {

            val t = distFromNet / halfTableLength
            val heightAtNet = netHeight + 35f
            val heightAtHit = 40f

            heightAtNet + (heightAtHit - heightAtNet) * t
        }
    }
}