package com.paliaapk.phonefix.diagnostics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.paliaapk.phonefix.model.DiagnosticStatus
import com.paliaapk.phonefix.model.HardwareTestResult
import com.paliaapk.phonefix.model.HardwareTestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.sin
import kotlin.math.sqrt

class HardwareTestController(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    // Live sensor values
    val accelValues = MutableStateFlow(Triple(0f, 0f, 0f))
    val gyroValues = MutableStateFlow(Triple(0f, 0f, 0f))
    val compassAzimuth = MutableStateFlow(0f)
    val lightLux = MutableStateFlow(0f)
    val proximityDistance = MutableStateFlow(5f)
    val micDecibels = MutableStateFlow(0)
    val isTorchActive = MutableStateFlow(false)

    // Hardware test results
    private val _testResults = MutableStateFlow<Map<HardwareTestType, HardwareTestResult>>(
        HardwareTestType.values().associateWith { HardwareTestResult(it) }
    )
    val testResults: StateFlow<Map<HardwareTestType, HardwareTestResult>> = _testResults.asStateFlow()

    private var activeAudioTrack: AudioTrack? = null
    private var isRecordingMic = false

    fun updateResult(type: HardwareTestType, status: DiagnosticStatus, details: String, measurement: String = "") {
        val current = _testResults.value.toMutableMap()
        current[type] = HardwareTestResult(type, status, details, measurement)
        _testResults.value = current
    }

    // --- Vibration Test ---
    fun testVibration(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vm?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(400)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Audio Tone (Speaker / Earpiece) ---
    suspend fun playTestTone(isEarpiece: Boolean, durationMs: Int = 1200): Boolean = withContext(Dispatchers.IO) {
        try {
            stopTestTone()
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val sample = ShortArray(numSamples)
            val freq = if (isEarpiece) 1200.0 else 880.0

            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i * freq / sampleRate
                sample[i] = (sin(angle) * Short.MAX_VALUE * 0.75).toInt().toShort()
            }

            val usage = if (isEarpiece) AudioAttributes.USAGE_VOICE_COMMUNICATION else AudioAttributes.USAGE_MEDIA
            val contentType = if (isEarpiece) AudioAttributes.CONTENT_TYPE_SPEECH else AudioAttributes.CONTENT_TYPE_MUSIC

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(usage)
                        .setContentType(contentType)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(sample, 0, numSamples)
            activeAudioTrack = track
            track.play()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun stopTestTone() {
        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
            activeAudioTrack = null
        } catch (e: Exception) {
            // ignore
        }
    }

    // --- Flashlight / Torch ---
    fun toggleTorch(): Boolean {
        return try {
            val cm = cameraManager ?: return false
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                        chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: cm.cameraIdList.firstOrNull() ?: return false

            val newState = !isTorchActive.value
            cm.setTorchMode(cameraId, newState)
            isTorchActive.value = newState
            true
        } catch (e: Exception) {
            false
        }
    }

    fun turnOffTorch() {
        if (isTorchActive.value) {
            try {
                val cm = cameraManager ?: return
                val cameraId = cm.cameraIdList.firstOrNull() ?: return
                cm.setTorchMode(cameraId, false)
                isTorchActive.value = false
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // --- Camera Hardware Check ---
    fun checkCameras(): Pair<HardwareTestResult, HardwareTestResult> {
        val cm = cameraManager
        if (cm == null) {
            val unavail = HardwareTestResult(HardwareTestType.REAR_CAMERA, DiagnosticStatus.UNAVAILABLE, "CameraManager unavailable")
            return Pair(unavail, unavail.copy(type = HardwareTestType.FRONT_CAMERA))
        }

        var backFound = false
        var frontFound = false
        var backDetails = "Not found"
        var frontDetails = "Not found"

        try {
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
                val megaPixels = if (sensorSize != null) {
                    "%.1f MP".format((sensorSize.width() * sensorSize.height()) / 1_000_000.0)
                } else "Sensor ready"

                if (facing == CameraCharacteristics.LENS_FACING_BACK && !backFound) {
                    backFound = true
                    backDetails = "Primary rear optical lens ($megaPixels), Flash: ${if (hasFlash) "Supported" else "No"}"
                } else if (facing == CameraCharacteristics.LENS_FACING_FRONT && !frontFound) {
                    frontFound = true
                    frontDetails = "Selfie front camera ($megaPixels)"
                }
            }
        } catch (e: Exception) {
            // catch
        }

        val backResult = if (backFound) {
            HardwareTestResult(HardwareTestType.REAR_CAMERA, DiagnosticStatus.PASS, backDetails, "Verified")
        } else {
            HardwareTestResult(HardwareTestType.REAR_CAMERA, DiagnosticStatus.ATTENTION, "Rear camera not detected", "Missing")
        }

        val frontResult = if (frontFound) {
            HardwareTestResult(HardwareTestType.FRONT_CAMERA, DiagnosticStatus.PASS, frontDetails, "Verified")
        } else {
            HardwareTestResult(HardwareTestType.FRONT_CAMERA, DiagnosticStatus.ATTENTION, "Front camera not detected", "Missing")
        }

        return Pair(backResult, frontResult)
    }

    // --- Charging / USB Status ---
    fun checkChargingUsb(): HardwareTestResult {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

            val plugName = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_USB -> "Connected to USB host"
                BatteryManager.BATTERY_PLUGGED_AC -> "Connected to AC power"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Connected to Wireless pad"
                else -> "USB / Power port disconnected"
            }

            HardwareTestResult(
                type = HardwareTestType.CHARGING_USB,
                status = DiagnosticStatus.PASS,
                details = "$plugName (${voltage}mV electrical line voltage)",
                measurement = if (plugged != 0) "Plugged in" else "Discharging"
            )
        } catch (e: Exception) {
            HardwareTestResult(HardwareTestType.CHARGING_USB, DiagnosticStatus.UNAVAILABLE, "Could not query battery electrical bus")
        }
    }

    // --- Sensor Listeners Start / Stop ---
    fun startSensorMonitoring() {
        val sm = sensorManager ?: return
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sm.getDefaultSensor(Sensor.TYPE_LIGHT)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensorMonitoring() {
        sensorManager?.unregisterListener(this)
        turnOffTorch()
        stopTestTone()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelValues.value = Triple(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroValues.value = Triple(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val azimuth = (Math.toDegrees(Math.atan2(event.values[0].toDouble(), event.values[1].toDouble())).toFloat() + 360) % 360
                compassAzimuth.value = azimuth
            }
            Sensor.TYPE_LIGHT -> {
                lightLux.value = event.values[0]
            }
            Sensor.TYPE_PROXIMITY -> {
                proximityDistance.value = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
