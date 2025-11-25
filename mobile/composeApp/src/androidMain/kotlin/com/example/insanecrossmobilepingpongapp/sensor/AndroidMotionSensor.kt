package com.example.insanecrossmobilepingpongapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.insanecrossmobilepingpongapp.model.DeviceOrientation
import com.example.insanecrossmobilepingpongapp.util.Log
import com.example.insanecrossmobilepingpongapp.util.ContextProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class AndroidMotionSensor(private val context: Context) : MotionSensor {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var updateCount = 0
    private val logEveryNUpdates = 50 // Log every 50 updates (~1 second at 50Hz)

    override val orientation: Flow<DeviceOrientation> = callbackFlow {
        Log.i(TAG, "🎮 Motion sensor starting...")
        Log.i(TAG, "📱 Rotation sensor available: ${rotationSensor != null}")
        Log.i(TAG, "📱 Accelerometer sensor available: ${accelerometerSensor != null}")

        // Store latest values from both sensors
        var latestPitch = 0f
        var latestRoll = 0f
        var latestYaw = 0f
        var latestAccelX = 0f
        var latestAccelY = 0f
        var latestAccelZ = 0f

        // Define sendCombinedData function BEFORE the listeners use it
        fun sendCombinedData() {
            val deviceOrientation = DeviceOrientation(
                pitch = latestPitch,
                roll = latestRoll,
                yaw = latestYaw,
                accelerationX = latestAccelX,
                accelerationY = latestAccelY,
                accelerationZ = latestAccelZ
            )

            // Periodic detailed logging
            updateCount++
            if (updateCount % logEveryNUpdates == 0) {
                val accelMagnitude = sqrt(
                    latestAccelX * latestAccelX +
                    latestAccelY * latestAccelY +
                    latestAccelZ * latestAccelZ
                )
                Log.d(TAG, "📊 Orientation → pitch: %.3f, roll: %.3f, yaw: %.3f".format(
                    Math.toDegrees(latestPitch.toDouble()),
                    Math.toDegrees(latestRoll.toDouble()),
                    Math.toDegrees(latestYaw.toDouble())
                ))
                Log.d(TAG, "🚀 Acceleration → X: %.2f, Y: %.2f, Z: %.2f, |mag|: %.2f m/s²".format(
                    latestAccelX, latestAccelY, latestAccelZ, accelMagnitude
                ))
            }

            trySend(deviceOrientation)
        }

        val rotationListener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    // Convert rotation vector to rotation matrix
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    // Get orientation angles from rotation matrix
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    latestPitch = orientationAngles[1]
                    latestRoll = orientationAngles[2]
                    latestYaw = orientationAngles[0]

                    sendCombinedData()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                val accuracyStr = when (accuracy) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> "UNRELIABLE"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM"
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH"
                    else -> "UNKNOWN"
                }
                Log.w(TAG, "⚠️ Rotation sensor accuracy changed: $accuracyStr")
            }
        }

        val accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                    // Linear acceleration excludes gravity
                    latestAccelX = event.values[0]
                    latestAccelY = event.values[1]
                    latestAccelZ = event.values[2]

                    sendCombinedData()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                val accuracyStr = when (accuracy) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> "UNRELIABLE"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM"
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH"
                    else -> "UNKNOWN"
                }
                Log.w(TAG, "⚠️ Accelerometer accuracy changed: $accuracyStr")
            }
        }

        // Register both sensors
        rotationSensor?.let {
            sensorManager.registerListener(
                rotationListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
            Log.i(TAG, "✅ Rotation sensor registered")
        }

        accelerometerSensor?.let {
            sensorManager.registerListener(
                accelerometerListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
            Log.i(TAG, "✅ Accelerometer sensor registered")
        }

        awaitClose {
            Log.i(TAG, "🛑 Motion sensor stopping...")
            sensorManager.unregisterListener(rotationListener)
            sensorManager.unregisterListener(accelerometerListener)
            Log.i(TAG, "✅ Sensors unregistered")
        }
    }

    override fun start() {
        Log.i(TAG, "start() called - Flow handles registration automatically")
    }

    override fun stop() {
        Log.i(TAG, "stop() called - Flow handles unregistration automatically")
    }

    override fun isAvailable(): Boolean {
        val available = rotationSensor != null && accelerometerSensor != null
        Log.i(TAG, "isAvailable() = $available")
        return available
    }

    companion object {
        private const val TAG = "AndroidMotionSensor"
    }
}


actual fun createMotionSensor(): MotionSensor {
    return AndroidMotionSensor(ContextProvider.getContext())
}
