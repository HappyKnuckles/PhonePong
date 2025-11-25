package com.example.insanecrossmobilepingpongapp.sensor

import com.example.insanecrossmobilepingpongapp.model.DeviceOrientation
import com.example.insanecrossmobilepingpongapp.util.Log
import com.example.insanecrossmobilepingpongapp.util.formatFloat
import com.example.insanecrossmobilepingpongapp.util.toDegrees
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.math.sqrt

class IosMotionSensor : MotionSensor {
    private val motionManager = CMMotionManager()
    private var updateCount = 0
    private val logEveryNUpdates = 50 // Log every 50 updates (~1 second at 50Hz)

    @OptIn(ExperimentalForeignApi::class)
    override val orientation: Flow<DeviceOrientation> = callbackFlow {
        Log.i(TAG, "🎮 Motion sensor starting...")
        Log.i(TAG, "📱 Device motion available: ${motionManager.deviceMotionAvailable}")

        if (!motionManager.deviceMotionAvailable) {
            Log.e(TAG, "❌ Device motion not available!")
            close(IllegalStateException("Device motion not available"))
            return@callbackFlow
        }

        motionManager.deviceMotionUpdateInterval = 0.02 // 50Hz, similar to Android SENSOR_DELAY_GAME
        Log.i(TAG, "⏱️ Update interval set to 50Hz (0.02s)")

        motionManager.startDeviceMotionUpdatesToQueue(
            NSOperationQueue.mainQueue
        ) { motion, error ->
            if (error != null) {
                Log.e(TAG, "❌ Motion sensor error: ${error.localizedDescription}")
                close(Exception("Motion sensor error: ${error.localizedDescription}"))
                return@startDeviceMotionUpdatesToQueue
            }

            motion?.let {
                // CMDeviceMotion provides attitude with pitch, roll, and yaw
                val attitude = it.attitude
                
                // CMDeviceMotion also provides userAcceleration (excludes gravity)
                // userAcceleration is a CValue<CMAcceleration>, so we use useContents
                val (accelX, accelY, accelZ) = it.userAcceleration.useContents {
                    Triple(x, y, z)
                }

                // Convert G-force to m/s² (1G ≈ 9.81 m/s²)
                val gravity = 9.81f
                val deviceOrientation = DeviceOrientation(
                    pitch = attitude.pitch.toFloat(),
                    roll = attitude.roll.toFloat(),
                    yaw = attitude.yaw.toFloat(),
                    accelerationX = (accelX * gravity).toFloat(),
                    accelerationY = (accelY * gravity).toFloat(),
                    accelerationZ = (accelZ * gravity).toFloat()
                )

                // Periodic detailed logging
                updateCount++
                if (updateCount % logEveryNUpdates == 0) {
                    val accelMagnitude = sqrt(
                        (accelX * gravity) * (accelX * gravity) +
                        (accelY * gravity) * (accelY * gravity) +
                        (accelZ * gravity) * (accelZ * gravity)
                    ).toFloat()

                    Log.d(TAG, "📊 Orientation → pitch: ${formatFloat(toDegrees(attitude.pitch).toFloat(), 3)}, roll: ${formatFloat(toDegrees(attitude.roll).toFloat(), 3)}, yaw: ${formatFloat(toDegrees(attitude.yaw).toFloat(), 3)}")
                    Log.d(TAG, "🚀 Acceleration → X: ${formatFloat((accelX * gravity).toFloat(), 2)}, Y: ${formatFloat((accelY * gravity).toFloat(), 2)}, Z: ${formatFloat((accelZ * gravity).toFloat(), 2)}, |mag|: ${formatFloat(accelMagnitude, 2)} m/s²")
                }

                trySend(deviceOrientation)
            }
        }

        Log.i(TAG, "✅ Device motion updates started")

        awaitClose {
            Log.i(TAG, "🛑 Motion sensor stopping...")
            motionManager.stopDeviceMotionUpdates()
            Log.i(TAG, "✅ Device motion updates stopped")
        }
    }

    override fun start() {
        Log.i(TAG, "start() called - Flow handles registration automatically")
    }

    override fun stop() {
        Log.i(TAG, "stop() called - Flow handles unregistration automatically")
    }

    override fun isAvailable(): Boolean {
        val available = motionManager.deviceMotionAvailable
        Log.i(TAG, "isAvailable() = $available")
        return available
    }

    companion object {
        private const val TAG = "IosMotionSensor"
    }
}

actual fun createMotionSensor(): MotionSensor {
    return IosMotionSensor()
}
